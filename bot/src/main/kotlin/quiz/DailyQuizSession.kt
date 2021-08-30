package quiz

import com.soywiz.klock.DateTimeTz
import models.Question
import models.TypeOfTest
import models.User
import storage.GoogleDriveReportStorage
import storage.ReportStorage
import storage.users.UserStorage
import telegram.*
import java.util.*


class DailyQuizSession(
    user: User,
    roomId: RoomId,
    chatId: ChatId,
    val dayTime: Time,
    userConnection: UserConnection,
    userStorage: UserStorage,
    reportStorage: ReportStorage,
    private val dailyQuizData: DailyQuizData,
    onEndedCallback: OnEnded
) : TelegramSession<Unit>(
    user = user,
    roomId = roomId,
    chatId = chatId,
    type = TypeOfTest.DailyQuiz,
    userConnection = userConnection,
    userStorage = userStorage,
    reportStorage = reportStorage,
    onEndedCallback = onEndedCallback
) {
    enum class Time { MORNING, EVENING }

    override suspend fun executeTesting(user: User, chatId: Long) {
        val data: DailyQuizData = dailyQuizData

        val questions: List<Question> = when (dayTime) {
            Time.MORNING -> data.morningQuestionsClosed + data.morningQuestionsOpen
            Time.EVENING -> data.eveningQuestionsClosed + data.eveningQuestionsOpen
        }

        val answers = collectAnswers(questions)

        val dailyQuizAnswers = DailyQuizAnswersContainer(
            user = user,
            date = DateTimeTz.nowLocal(),
            answers = answers
        )
        userStorage.saveDailyQuizAnswers(
            user = user,
            answers = dailyQuizAnswers,
        )
        userConnection.cleanUp(
            chatId = chatId,
            messageIds = state.messageIds
        )
        onEndedCallback(this)
    }

    private suspend fun collectAnswers(questions: List<Question>): List<DailyQuizAnswer> {
        val questionsQueue: Queue<Question> = LinkedList(questions)

        val sentQuestions = sortedMapOf<MessageId, Question>()
        val answers = mutableMapOf<MessageId, DailyQuizAnswer?>()

        askNextQuestion(questionsQueue, sentQuestions)

        while (answers.size < questions.size) {

            val answerEvent: AnswerEvent = waitForAnswer()

            answers[answerEvent.messageId] = when (answerEvent.userAnswer) {
                is UserAnswer.DailyQuiz -> handleClosedAnswer(
                    sentQuestions = sentQuestions,
                    answerEvent = answerEvent,
                    questionsQueue = questionsQueue
                )
                is UserAnswer.Text -> handleOpenAnswer(
                    sentQuestions = sentQuestions,
                    answerEvent = answerEvent,
                    questionsQueue = questionsQueue
                )
                is UserAnswer.Skip -> handleSkipButton(
                    questionsQueue = questionsQueue,
                    sentQuestions = sentQuestions
                )
                else -> null
            }
        }
        return answers.values.filterNotNull().toList()
    }

    private fun handleSkipButton(
        questionsQueue: Queue<Question>,
        sentQuestions: SortedMap<MessageId, Question>
    ): Nothing? {
        askNextQuestion(questionsQueue, sentQuestions)
        return null
    }

    private fun handleOpenAnswer(
        sentQuestions: SortedMap<MessageId, Question>,
        answerEvent: AnswerEvent,
        questionsQueue: Queue<Question>
    ): DailyQuizAnswer {
        askNextQuestion(questionsQueue, sentQuestions)

        state.addMessageId(answerEvent.messageId)

        return DailyQuizAnswer.Text(
            questionIndex = sentQuestions.values.last().index,
            questionText = sentQuestions.values.last().text,
            text = (answerEvent.userAnswer as UserAnswer.Text).text
        )
    }

    private fun handleClosedAnswer(
        sentQuestions: MutableMap<MessageId, Question>,
        answerEvent: AnswerEvent,
        questionsQueue: Queue<Question>
    ): DailyQuizAnswer {

        val buttonQuestion = sentQuestions[answerEvent.messageId]!!
        highLightAnswer(answerEvent, buttonQuestion)

        if (isLastAsked(answerEvent.messageId)) {
            askNextQuestion(questionsQueue, sentQuestions)
        }
        return DailyQuizAnswer.Option(
            questionIndex = buttonQuestion.index,
            questionText = buttonQuestion.text,
            option = (answerEvent.userAnswer as UserAnswer.DailyQuiz).answer
        )
    }

    private fun askNextQuestion(
        questionsQueue: Queue<Question>,
        sentQuestions: MutableMap<MessageId, Question>
    ) {
        questionsQueue.poll()?.let { question: Question ->
            val messageId = sendQuestion(question)
            sentQuestions.put(messageId, question)
        }
    }

    private fun highLightAnswer(buttonClick: AnswerEvent, question: Question) {
        buttonClick.userAnswer as UserAnswer.DailyQuiz

        val index = question.options.indexOfFirst {
            it.tag == buttonClick.userAnswer.answer.name
        }
        userConnection.highlightAnswer(
            messageId = buttonClick.messageId,
            chatId = chatId,
            buttons = buttons(question.options),
            buttonToHighLight = index
        )
    }

    private fun isLastAsked(messageId: MessageId): Boolean {
        val lastMessageId: MessageId? = state.messageIds.lastOrNull()
        return messageId == lastMessageId
    }

    private fun sendQuestion(question: Question): MessageId {
        val buttons = buttons(question.options)

        val messageId: MessageId = if (buttons.isNotEmpty()) {
            userConnection.sendMessageWithButtons(
                chatId = chatId,
                text = question.text,
                buttons = buttons(question.options)
            )
        } else {
            userConnection.sendMessage(
                chatId = chatId,
                text = question.text
            )
        }.also { state.addMessageId(it) }

        return messageId
    }

    private fun buttons(options: List<Question.Option>): List<Button> {
        val buttons = options.map { option ->
            Button(
                text = option.text,
                userAnswer = UserAnswer.DailyQuiz(DailyQuizOptions.valueOf(option.tag))
            )
        }
        return buttons
    }
}