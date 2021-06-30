package mmpi.telegram

import Gender
import kotlinx.coroutines.channels.Channel
import mmpi.*
import models.TypeOfTest
import models.User
import storage.CentralDataStorage
import telegram.helpers.showResult
import Result
import com.soywiz.klock.DateTimeTz
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import telegram.*

private typealias OnAnswerReceived = suspend (callback: Callback, messageId: MessageId?) -> Result<MessageId>
/**For testing only*/
private typealias OnFinished = ((answers: List<MmpiProcess.Answer>) -> Unit)?

class MmpiSession(
    override val user: User,
    override val roomId: RoomId,
    override val chatId: ChatId,
    override val type: TypeOfTest,
    userConnection: UserConnection,
    override val onEndedCallback: OnEnded
) : TelegramSession<Long>(user, chatId, roomId, type, userConnection, onEndedCallback) {

    companion object {
        val scope = GlobalScope
    }

    internal var testingCallback: OnFinished = null
    private var onAnswer: OnAnswerReceived? = null
    private var ongoingProcess: MmpiProcess? = null

    override suspend fun start() {
        val handler = CoroutineExceptionHandler { _, exception ->
            print(exception)
            userConnection.notifyAdmin("MmpiSession error", exception)
            userConnection.sendMessage(chatId, CentralDataStorage.string("start_again"))
        }
        scope.launch(handler) { executeTesting(user) }
    }

    private suspend fun executeTesting(user: User) {
        askGender(
            userId = sessionId,
            connection = userConnection
        )
        val gender = waitForGenderChosen()
        ongoingProcess = MmpiProcess(gender, type)

        collectAllAnswers(ongoingProcess!!, user, gender)
    }

    private suspend fun waitForGenderChosen(): Gender {
        val gChannel = Channel<Gender>(1)
        onAnswer = { callback: Callback, messageId: MessageId? ->
            callback as Callback.GenderAnswer

            onAnswer = null

            messageId?.let {
                userConnection.highlightAnswer(it, callback)
            }
            gChannel.offer(callback.answer)
            Result.Success(0)
        }
        return gChannel.receive()
    }

    private var lastQuestionId = -1L
    private suspend fun collectAllAnswers(
        ongoingProcess: MmpiProcess,
        user: User,
        gender: Gender
    ) {
        lastQuestionId = sendFirstQuestion(ongoingProcess, userConnection)

        onAnswer = { callback: Callback, messageId: MessageId? ->
            callback as Callback.MmpiAnswer
            userConnection.highlightAnswer(messageId, callback)

            ongoingProcess.submitAnswer(
                callback.index, callback.answer
            )
            if (ongoingProcess.hasNextQuestion()
                && ongoingProcess.isItLastAskedQuestion(callback.index)
            ) {
                lastQuestionId = sendNextQuestion(ongoingProcess, userConnection)
            }
            if (ongoingProcess.allQuestionsAreAnswered()) {
                finishTesting(ongoingProcess, user, gender, userConnection)
            }
            Result.Success(messageId ?: -1L)
        }
    }

    override suspend fun applyState(state: SessionState) {
        super.applyState(state)
//        lastQuestionId = state.answers.maxOfOrNull { it.messageId + 1 } ?: -1L

//        lastQuestionId = state.answers
//            .filterIsInstance(Callback.MmpiAnswer::class.java)
//            .maxOfOrNull { (it as Callback.MmpiAnswer).index }

        state.answers
            .filterIsInstance(Callback.MmpiAnswer::class.java)
            .maxOfOrNull { it.index }


        ongoingProcess?.setNextQuestionIndex(state.answers.size - 1)
    }

    private suspend fun finishTesting(
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
            answersList = ongoingProcess.answers.values.toList()
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
        testingCallback?.invoke(ongoingProcess.answers.values.toList())
    }

    private fun sendFirstQuestion(ongoingProcess: MmpiProcess, userConnection: UserConnection) =
        sendNextQuestion(ongoingProcess, userConnection)

    private fun sendNextQuestion(
        ongoingProcess: MmpiProcess,
        userConnection: UserConnection
    ): MessageId {
        val question = ongoingProcess.nextQuestion()
        val messageId = userConnection.sendMessageWithButtons(
            chatId = sessionId,
            text = question.text,
            buttons = mmpiButtons(question)
        )
        return messageId
    }

    private val mutex = Mutex()
    override suspend fun onAnswer(callback: Callback, messageId: MessageId?): Result<Long> {
        mutex.withLock {
            while (onAnswer == null) {
                delay(1)
            }
            return onAnswer?.invoke(callback, messageId) ?: Result.Error("onAnswer is null")
        }
    }
}