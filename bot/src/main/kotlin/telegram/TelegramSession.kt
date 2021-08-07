package telegram

import models.User
import Result
import kotlinx.coroutines.channels.Channel
import models.TypeOfTest

typealias OnEnded = (TelegramSession<Any>) -> Unit
typealias MessageId = Long
typealias RoomId = Long
typealias ChatId = Long
typealias UserId = Long
typealias SessionId = Long


abstract class TelegramSession<out T>(
    val user: User,
    val roomId: RoomId,
    val chatId: ChatId,
    val type: TypeOfTest,
    val userConnection: UserConnection,
    val onEndedCallback: OnEnded
) {
    val sessionId by lazy { user.id }

    val state by lazy {
        SessionState(
            userId = user.id,
            sessionId = user.id,
            chatId = chatId,
            roomId = roomId,
            type = type,
        )
    }

    abstract suspend fun start()

    private val answers = Channel<QuizButtonClick>(10)

    suspend fun sendAnswer(quizButton: QuizButton, messageId: MessageId): Result<Unit> {
        state.saveAnswer(quizButton)
        answers.offer(QuizButtonClick(quizButton, messageId))
        return Result.Success(Unit)
    }

    suspend fun waitForAnswer(): QuizButtonClick {
        return answers.receive()
    }

    open suspend fun applyState(state: SessionState) {
        userConnection.pause()
        start()
        state.answers.forEach {
            sendAnswer(it, NOT_SENT)
        }
        this.state.addMessageIds(state.messageIds)
        userConnection.resume()
    }
}
