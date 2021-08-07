package mmpi.telegram

import Gender
import mmpi.*
import models.TypeOfTest
import models.User
import storage.CentralDataStorage
import telegram.helpers.showResult
import Result
import com.soywiz.klock.DateTimeTz
import kotlinx.coroutines.*
import telegram.*

/**For testing only*/
private typealias OnFinished = ((answers: List<MmpiProcess.Answer>) -> Unit)?

class MmpiSession(
    user: User,
    roomId: RoomId,
    chatId: ChatId,
    type: TypeOfTest,
    userConnection: UserConnection,
    onEndedCallback: OnEnded
) : TelegramSession<Long>(
    user = user,
    roomId = roomId,
    chatId = chatId,
    type = type,
    userConnection = userConnection,
    onEndedCallback = onEndedCallback
) {
    companion object {
        val scope = GlobalScope
    }

    internal var testingCallback: OnFinished = null
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
        return (waitForAnswer().quizButton as QuizButton.GenderAnswer).answer
    }

    private suspend fun collectAllAnswers(
        ongoingProcess: MmpiProcess,
        user: User,
        gender: Gender
    ) {
        sendFirstQuestion(ongoingProcess, userConnection)
            .apply { state.addMessageId(this) }

        while (ongoingProcess.hasNextQuestion()) {
            val quizButtonClick = waitForAnswer()
            val mmpiButton = quizButtonClick.quizButton as QuizButton.Mmpi

            ongoingProcess.submitAnswer(
                mmpiButton.index, mmpiButton.answer
            )
            val question = ongoingProcess.questions[mmpiButton.index]
            val index = question.options.indexOfFirst {
                it.tag == mmpiButton.answer.name
            }
            userConnection.highlightAnswer(
                messageId = quizButtonClick.messageId,
                chatId = chatId,
                buttons = mmpiButtons(question),
                buttonToHighLight = index
            )
            if (//ongoingProcess.hasNextQuestion() &&
                ongoingProcess.isItLastAskedQuestion(mmpiButton.index)
            ) {
                sendNextQuestion(ongoingProcess, userConnection)
                    .apply { state.addMessageId(this) }
            }
            if (ongoingProcess.allQuestionsAreAnswered()) {
                finishTesting(ongoingProcess, user, gender, userConnection)
            }
        }
    }

    override suspend fun applyState(state: SessionState) {
        super.applyState(state)

        val index: Int = state.answers
            .filterIsInstance(QuizButton.Mmpi::class.java)
            .maxOfOrNull { it.index } ?: 0

        ongoingProcess?.setNextQuestionIndex(index + 1)
    }

    private suspend fun finishTesting(
        ongoingProcess: MmpiProcess,
        user: User,
        gender: Gender,
        userConnection: UserConnection,
    ): Result<Unit> {
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
}