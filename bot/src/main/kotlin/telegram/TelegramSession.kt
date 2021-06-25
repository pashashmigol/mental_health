package telegram

import models.User
import Result
import models.TypeOfTest


typealias OnEnded = (TelegramSession<Any>) -> Unit

abstract class TelegramSession<out T>(
    open val user: User,
    open val roomId: Long,
    open val chatId: Long,
    open val type: TypeOfTest
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

    suspend fun sendAnswer(messageId: Long, data: String): Result<T> {
        state.addAnswer(messageId, data)
        return onAnswer(messageId, data)
    }

    abstract suspend fun onAnswer(messageId: Long, data: String): Result<T>

    suspend fun applyState(state: SessionState) {
        start()
        state.messages.forEach {
            sendAnswer(it.messageId, it.data)
        }
    }
}
