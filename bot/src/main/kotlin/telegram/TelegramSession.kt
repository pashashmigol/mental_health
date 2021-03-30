package telegram

import com.github.kotlintelegrambot.Bot
import models.User

interface TelegramSession {
    val id: Long
    fun onCallbackFromUser(messageId: Long, data: String)
    fun start(user: User, chatId: Long)
}

typealias OnEnded = (TelegramSession) -> Unit


