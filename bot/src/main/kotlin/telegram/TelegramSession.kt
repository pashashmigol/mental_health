package telegram

import models.User
import Result
import models.TypeOfTest


typealias OnEnded = (TelegramSession<Any>) -> Unit

abstract class TelegramSession<out T>(
    open val id: Long,
    open val type: TypeOfTest
) {
    val state by lazy { SessionState(id, type) }

    abstract suspend fun start(user: User, chatId: Long)

    open suspend fun onCallbackFromUser(messageId: Long, data: String): Result<T> {
        state.add(messageId, data)
        return Result.Error("TelegramSession.onCallbackFromUser() should be overridden!")
    }

    suspend fun applyState(state: SessionState) {
        state.messages.forEach {
            onCallbackFromUser(it.messageId, it.data)
        }
    }
}
