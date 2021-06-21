package telegram

import models.User
import Result


typealias OnEnded = (TelegramSession<Any>) -> Unit

abstract class TelegramSession<out T>(open val id: Long) {
    private val state by lazy { State(id) }

    abstract suspend fun start(user: User, chatId: Long)

    open suspend fun onCallbackFromUser(messageId: Long, data: String): Result<T> {
        state.add(messageId, data)
        return Result.Error("TelegramSession.onCallbackFromUser() should be overridden!")
    }

    suspend fun applyState(state: State) {
        state.messages.forEach {
            onCallbackFromUser(it.messageId, it.data)
        }
    }
}
