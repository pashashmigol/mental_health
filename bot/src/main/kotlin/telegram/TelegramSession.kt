package telegram

import models.User
import Result


interface TelegramSession<out T> {
    val id: Long
    suspend fun onCallbackFromUser(messageId: Long, data: String): Result<T>
    suspend fun start(user: User, chatId: Long)
}

typealias OnEnded = (TelegramSession<Any>) -> Unit


