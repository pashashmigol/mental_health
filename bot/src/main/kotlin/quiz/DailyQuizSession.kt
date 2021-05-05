package quiz

import Result
import models.TypeOfTest
import models.User
import storage.CentralDataStorage
import telegram.*

class DailyQuizSession(
    override val user: User,
    override val roomId: RoomId,
    override val chatId: ChatId,
    userConnection: UserConnection,
    override val onEndedCallback: OnEnded
) : TelegramSession<Long>(user, chatId, roomId, TypeOfTest.DailyQuiz, userConnection, onEndedCallback) {

    override suspend fun start() {
        val data: DailyQuizData = CentralDataStorage.dailyQuizData

        data.questions.forEach { question ->
            userConnection.sendMessageWithButtons(
                chatId = 0,
                text = question.text,
                buttons = DailyQuizAnswer.values().map { answer: DailyQuizAnswer ->
                    Button(
                        text = answer.title,
                        callback = Callback.DailyQuiz(answer)
                    )
                },
                placeButtonsVertically = false
            )
        }
    }

    override suspend fun onAnswer(callback: Callback, messageId: MessageId?): Result<Long> {
        TODO("Not yet implemented")
    }
}