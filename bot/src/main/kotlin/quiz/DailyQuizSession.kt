package quiz

import com.soywiz.klock.DateTimeTz
import models.Question
import models.TypeOfTest
import models.User
import storage.CentralDataStorage
import telegram.*
import java.util.*


class DailyQuizSession(
    user: User,
    roomId: RoomId,
    chatId: ChatId,
    val dayTime: Time,
    userConnection: UserConnection,
    onEndedCallback: OnEnded
) : TelegramSession<Unit>(
    user = user,
    roomId = roomId,
    chatId = chatId,
    type = TypeOfTest.DailyQuiz,
    userConnection = userConnection,
    onEndedCallback = onEndedCallback
) {
    enum class Time { MORNING, EVENING }

    override suspend fun executeTesting(user: User, chatId: Long) {
        val data: DailyQuizData = CentralDataStorage.dailyQuizData

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
        CentralDataStorage.usersStorage.saveDailyQuizAnswers(
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

        val sentQuestions = mutableMapOf<MessageId, Question>()
        val answers = mutableMapOf<MessageId, DailyQuizAnswer>()

        askNextQuestion(questionsQueue, sentQuestions)

        while (answers.size < questions.size) {
            val buttonClick: QuizButtonClick = waitForAnswer()

            if (buttonClick.quizButton is QuizButton.DailyQuiz) {
                val buttonQuestion = sentQuestions[buttonClick.messageId]!!
                highLightAnswer(buttonClick, buttonQuestion)

                answers[buttonClick.messageId] = DailyQuizAnswer.Option(
                    questionIndex = buttonQuestion.index,
                    questionText = buttonQuestion.text,
                    option = buttonClick.quizButton.answer
                )
            }
            if (isLastAsked(buttonClick.messageId)) {
                askNextQuestion(questionsQueue, sentQuestions)
            }
        }
        return answers.values.toList()
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

    private fun highLightAnswer(buttonClick: QuizButtonClick, question: Question) {
        buttonClick.quizButton as QuizButton.DailyQuiz

        val index = question.options.indexOfFirst {
            it.tag == buttonClick.quizButton.answer.name
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
                quizButton = QuizButton.DailyQuiz(DailyQuizOptions.valueOf(option.tag))
            )
        }
        return buttons
    }
}