package mmpi.telegram

import Gender
import kotlinx.coroutines.channels.Channel
import mmpi.*
import mmpi.report.generateReport
import models.Type
import models.User
import storage.CentralDataStorage
import telegram.OnEnded
import telegram.TelegramSession
import telegram.UserConnection
import telegram.helpers.showResult
import Result
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

private typealias OnAnswerReceived = (messageId: Long, answer: String) -> Result<Int>
private typealias OnFinishedForTestingOnly = ((answers: List<MmpiProcess.Answer>) -> Unit)?

class MmpiSession(
    override val id: Long,
    private val type: Type,
    val userConnection: UserConnection,
    val onEndedCallback: OnEnded
) : TelegramSession<Int> {
    companion object {
        val scope = GlobalScope
    }

    internal var testingCallback: OnFinishedForTestingOnly = null
    private var onAnswer: OnAnswerReceived? = null

    override suspend fun start(user: User, chatId: Long) {
        val handler = CoroutineExceptionHandler { _, exception ->
            userConnection.notifyAdmin("MmpiSession error", exception)
        }
        scope.launch(handler) { executeTesting(user) }
    }

    private suspend fun executeTesting(user: User) {
        askGender(
            userId = id,
            question = createGenderQuestion(),
            connection = userConnection
        )
        val gender = waitForGenderChosen()
        val ongoingProcess = MmpiProcess(gender, type)

        collectAllAnswers(ongoingProcess, user)
    }

    private suspend fun waitForGenderChosen(): Gender {
        val gChannel = Channel<Gender>(1)
        onAnswer = { messageId: Long, answer: String ->
            onAnswer = null

            if (Gender.names.contains(answer)) {
                userConnection.highlightAnswer(messageId, answer)
                val gender = Gender.valueOf(answer)
                gChannel.offer(gender)
                Result.Success(0)
            } else {
                Result.Error("\"$answer\" is not gender")
            }
        }
        return gChannel.receive()
    }

    private fun collectAllAnswers(ongoingProcess: MmpiProcess, user: User) {
        val mesIdToIndex = ConcurrentHashMap<Long, Int>()

        sendFirstQuestion(ongoingProcess, userConnection).apply {
            mesIdToIndex[first] = second
        }

        onAnswer = { mesId: Long, answer: String ->
            val index = mesIdToIndex[mesId]

            if (index != null) {
                userConnection.highlightAnswer(mesId, answer)

                ongoingProcess.submitAnswer(
                    index, MmpiProcess.Answer.valueOf(answer)
                )

                if (ongoingProcess.hasNextQuestion()
                    && ongoingProcess.isItLastAskedQuestion(index)
                ) {
                    sendNextQuestion(ongoingProcess, userConnection).apply {
                        mesIdToIndex[first] = second
                    }
                }
                if (ongoingProcess.allQuestionsAreAnswered()) {
                    userConnection.cleanUp()
                    finishTesting(ongoingProcess, user, userConnection)
                }

                Result.Success(index)
            } else {
                Result.Error("No message with id $mesId")
            }
        }
    }

    private fun finishTesting(
        ongoingProcess: MmpiProcess,
        user: User,
        userConnection: UserConnection,
    ) {
        onAnswer = null
        val result = ongoingProcess.calculateResult()

        val report = generateReport(
            userId = user.name,
            questions = ongoingProcess.questions,
            answers = ongoingProcess.answers,
            result = result
        )
        val resultFolder = CentralDataStorage.reports.saveMmpi(
            userId = user.name,
            report = report,
            type = type
        )
        showResult(
            user = user,
            resultLink = resultFolder,
            userConnection = userConnection
        )
        onEndedCallback(this)
        testingCallback?.invoke(ongoingProcess.answers)
    }

    private fun sendFirstQuestion(ongoingProcess: MmpiProcess, userConnection: UserConnection) =
        sendNextQuestion(ongoingProcess, userConnection)

    private fun sendNextQuestion(
        ongoingProcess: MmpiProcess,
        userConnection: UserConnection
    ): Pair<Long, Int> {
        val question = ongoingProcess.nextQuestion()
        val messageId = userConnection.sendMessageWithButtons(
            chatId = id,
            text = question.text,
            buttons = buttons(question)
        )
        return Pair(messageId, question.index)
    }

    private val mutex = Mutex()
    override suspend fun onCallbackFromUser(messageId: Long, data: String): Result<Int> {
        mutex.withLock {
            while (onAnswer == null) {
                delay(1)
            }
            return onAnswer?.invoke(messageId, data) ?: Result.Error("onAnswer is null")
        }
    }
}