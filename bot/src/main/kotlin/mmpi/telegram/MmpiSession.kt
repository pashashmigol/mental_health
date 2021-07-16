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
        state.addToStorage()
        scope.launch(handler) { executeTesting(user) }
    }

    private suspend fun executeTesting(user: User) {
        askGender(
            userId = sessionId,
            connection = userConnection
        ).apply { state.addMessageId(this) }

        val gender = waitForGenderChosen()
        ongoingProcess = MmpiProcess(gender, type)

        collectAllAnswers(ongoingProcess!!, user, gender)
    }

    private suspend fun waitForGenderChosen(): Gender {
        val gChannel = Channel<Gender>(1)

        onAnswer = { callback: Callback, messageId: MessageId? ->
            callback as Callback.GenderAnswer
            onAnswer = null

            val index = Gender.values().indexOfFirst {
                it == callback.answer
            }
            messageId?.let {
                userConnection.highlightAnswer(
                    messageId = messageId,
                    chatId = chatId,
                    buttons = genderButtons(),
                    buttonToHighLight = index
                )
            }
            gChannel.offer(callback.answer)
            Result.Success(0)
        }
        return gChannel.receive()
    }

    private suspend fun collectAllAnswers(
        ongoingProcess: MmpiProcess,
        user: User,
        gender: Gender
    ) {
        sendFirstQuestion(ongoingProcess, userConnection)
            .apply { state.addMessageId(this) }

        onAnswer = { callback: Callback, messageId: MessageId? ->
            callback as Callback.MmpiAnswer

            val question = ongoingProcess.questions[callback.index]

            val index = question.options.indexOfFirst {
                it.tag == callback.answer.name
            }
            userConnection.highlightAnswer(
                messageId = messageId,
                chatId = chatId,
                buttons = mmpiButtons(question),
                buttonToHighLight = index
            )
            ongoingProcess.submitAnswer(
                callback.index, callback.answer
            )
            if (ongoingProcess.hasNextQuestion()
                && ongoingProcess.isItLastAskedQuestion(callback.index)
            ) {
                sendNextQuestion(ongoingProcess, userConnection)
                    .apply { state.addMessageId(this) }
            }
            if (ongoingProcess.allQuestionsAreAnswered()) {
                finishTesting(ongoingProcess, user, gender, userConnection)
            }
            Result.Success(messageId ?: NOT_SENT)
        }
    }

    override suspend fun applyState(state: SessionState) {
        super.applyState(state)

        val index: Int = state.answers
            .filterIsInstance(Callback.MmpiAnswer::class.java)
            .maxOfOrNull { it.index } ?: 0

        ongoingProcess?.setNextQuestionIndex(index + 1)
    }

    private suspend fun finishTesting(
        ongoingProcess: MmpiProcess,
        user: User,
        gender: Gender,
        userConnection: UserConnection,
    ): Result<Unit> {
        onAnswer = null
        val result = ongoingProcess.calculateResult()

        val answers = MmpiAnswers(
            user = user,
            date = DateTimeTz.nowLocal(),
            gender = gender,
            answersList = ongoingProcess.answers.values.toList()
        )
        val parentFolder = CentralDataStorage.saveMmpi(
            user = user,
            typeOfTest = type,
            questions = ongoingProcess.questions,
            answers = answers,
            result = result,
            saveAnswers = true
        ).dealWithError { return it }

        showResult(
            user = user,
            resultLink = parentFolder.link,
            userConnection = userConnection
        )
        userConnection.cleanUp(chatId, state.messageIds)
        onEndedCallback(this)

        testingCallback?.invoke(ongoingProcess.answers.values.toList())

        return Result.Success(Unit)
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