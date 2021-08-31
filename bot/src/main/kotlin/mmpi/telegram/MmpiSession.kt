package mmpi.telegram

import Gender
import mmpi.*
import models.TypeOfTest
import models.User
import telegram.helpers.showResult
import Result
import StoragePack
import com.soywiz.klock.DateTimeTz
import storage.users.saveMmpi
import telegram.*

/**For testing only*/
private typealias OnFinished = ((answers: List<MmpiProcess.Answer>) -> Unit)?

class MmpiSession(
    user: User,
    roomId: RoomId,
    chatId: ChatId,
    type: TypeOfTest,
    userConnection: UserConnection,
    storagePack: StoragePack,
    private val mmpiData: MmpiData,
    onEndedCallback: OnEnded
) : TelegramSession<Long>(
    user = user,
    roomId = roomId,
    chatId = chatId,
    type = type,
    userConnection = userConnection,
    storagePack = storagePack,
    onEndedCallback = onEndedCallback
) {
    internal var testingCallback: OnFinished = null
    private var ongoingProcess: MmpiProcess? = null

    override suspend fun executeTesting(user: User, chatId: Long) {
        askGender(
            userId = sessionId,
            connection = userConnection
        ).apply { state.addMessageId(this) }

        val gender = waitForGenderChosen()
        ongoingProcess = MmpiProcess(gender, type, mmpiData)

        collectAllAnswers(ongoingProcess!!, user, gender)
    }

    private suspend fun waitForGenderChosen(): Gender {
        return (waitForAnswer().userAnswer as UserAnswer.GenderAnswer).answer
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
            val mmpiButton = quizButtonClick.userAnswer as UserAnswer.Mmpi

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
            if (ongoingProcess.itLastAskedQuestion(mmpiButton.index)) {
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
            .filterIsInstance(UserAnswer.Mmpi::class.java)
            .maxOfOrNull { it.index } ?: 0

        ongoingProcess?.setNextQuestionIndex(index + 1)
    }

    private suspend fun finishTesting(
        ongoingProcess: MmpiProcess,
        user: User,
        gender: Gender,
        userConnection: UserConnection
    ): Result<Unit> {
        val result = ongoingProcess.calculateResult()

        val answers = MmpiAnswersContainer(
            user = user,
            date = DateTimeTz.nowLocal(),
            gender = gender,
            answersList = ongoingProcess.answers.values.toList()
        )
        val parentFolder = saveMmpi(
            user = user,
            typeOfTest = type,
            questions = ongoingProcess.questions,
            answers = answers,
            result = result,
            saveAnswers = true,
            answerStorage = storagePack.answerStorage,
            reportStorage = storagePack.reportStorage
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