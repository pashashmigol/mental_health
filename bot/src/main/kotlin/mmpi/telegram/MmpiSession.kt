package mmpi.telegram

import Gender
import kotlinx.coroutines.channels.Channel
import mmpi.*
import models.TypeOfTest
import models.User
import storage.CentralDataStorage
import telegram.OnEnded
import telegram.TelegramSession
import telegram.UserConnection
import telegram.helpers.showResult
import Result
import com.soywiz.klock.DateTimeTz
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

private typealias OnAnswerReceived = (messageId: Long, answer: String) -> Result<Int>
private typealias OnFinishedForTestingOnly = ((answers: List<MmpiProcess.Answer>) -> Unit)?

class MmpiSession(
    override val user: User,
    override val roomId: Long,
    override val chatId: Long,
    override val type: TypeOfTest,
    userConnection: UserConnection,
    override val onEndedCallback: OnEnded
) : TelegramSession<Int>(user, chatId, roomId, type, userConnection, onEndedCallback) {

    companion object {
        val scope = GlobalScope
    }

    internal var testingCallback: OnFinishedForTestingOnly = null
    private var onAnswer: OnAnswerReceived? = null

    override suspend fun start() {
        val handler = CoroutineExceptionHandler { _, exception ->
            userConnection.notifyAdmin("MmpiSession error", exception)
            userConnection.sendMessage(chatId, CentralDataStorage.string("start_again"))
        }
        scope.launch(handler) { executeTesting(user) }
    }

    private suspend fun executeTesting(user: User) {
        askGender(
            userId = sessionId,
            question = createGenderQuestion(),
            connection = userConnection
        )
        val gender = waitForGenderChosen()
        val ongoingProcess = MmpiProcess(gender, type)

        collectAllAnswers(ongoingProcess, user, gender)
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

    private fun collectAllAnswers(
        ongoingProcess: MmpiProcess,
        user: User,
        gender: Gender
    ) {
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
                    finishTesting(ongoingProcess, user, gender, userConnection)
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
        gender: Gender,
        userConnection: UserConnection,
    ) {
        onAnswer = null
        val result = ongoingProcess.calculateResult()

        val answers = MmpiAnswers(
            user = user,
            date = DateTimeTz.nowLocal(),
            gender = gender,
            answersList = ongoingProcess.answers
        )
        val resultFolder = CentralDataStorage.saveMmpi(
            user = user,
            typeOfTest = type,
            questions = ongoingProcess.questions,
            answers = answers,
            result = result,
            saveAnswers = true
        ) as Result.Success
        showResult(
            user = user,
            resultLink = resultFolder.data,
            userConnection = userConnection
        )
        userConnection.cleanUp()
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
            chatId = sessionId,
            text = question.text,
            buttons = buttons(question)
        )
        return Pair(messageId, question.index)
    }

    private val mutex = Mutex()
    override suspend fun onAnswer(messageId: Long, data: String): Result<Int> {
        mutex.withLock {
            while (onAnswer == null) {
                delay(1)
            }
            return onAnswer?.invoke(messageId, data) ?: Result.Error("onAnswer is null")
        }
    }
}