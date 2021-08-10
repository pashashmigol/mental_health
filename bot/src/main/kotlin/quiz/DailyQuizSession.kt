package quiz

import com.soywiz.klock.DateTimeTz
import models.Question
import models.TypeOfTest
import models.User
import storage.CentralDataStorage
import telegram.*


class DailyQuizSession(
    user: User,
    roomId: RoomId,
    chatId: ChatId,
    userConnection: UserConnection,
    onEndedCallback: OnEnded
) : TelegramSession<Unit>(
    user,
    chatId,
    roomId,
    TypeOfTest.DailyQuiz,
    userConnection,
    onEndedCallback
) {
    override suspend fun start() {
        val data: DailyQuizData = CentralDataStorage.dailyQuizData

        val answers = DailyQuizAnswers(
            user = user,
            date = DateTimeTz.nowLocal(),
            answers = data.morningQuestions.map {
                ask(it)
            }
        )
        CentralDataStorage.usersStorage.saveDailyQuizAnswers(
            user = user,
            answers = answers,
        )
        userConnection.cleanUp(
            chatId = chatId,
            messageIds = state.messageIds
        )
        data.eveningQuestions.forEach { ask(it) }
        userConnection.cleanUp(
            chatId = chatId,
            messageIds = state.messageIds
        )
        onEndedCallback(this)
    }

    private suspend fun ask(question: Question): DailyQuizAnswers.Answer {
        userConnection.sendMessageWithButtons(
            chatId = chatId,
            text = question.text,
            buttons = DailyQuizAnswers.Option.values().map { option ->
                Button(
                    text = option.title,
                    quizButton = QuizButton.DailyQuiz(option)
                )
            }
        ).let { state.addMessageId(it) }

        return DailyQuizAnswers.Answer(
            questionIndex = question.index,
            questionText= question.text,
            option = (waitForAnswer().quizButton as QuizButton.DailyQuiz).answer
        )
    }
}