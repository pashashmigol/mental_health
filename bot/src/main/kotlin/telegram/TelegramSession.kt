package telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import models.User

interface TelegramSession {
    val id: Long
    fun onCallbackFromUser(env: CallbackQueryHandlerEnvironment)
    fun start(user: User, chatId: Long, bot: Bot)
}

typealias OnEnded = (TelegramSession) -> Unit


