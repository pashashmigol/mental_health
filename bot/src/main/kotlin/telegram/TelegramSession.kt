package telegram

import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment

interface TelegramSession {
    val id: Long
    fun onCallbackFromUser(env: CallbackQueryHandlerEnvironment)
    fun start(env: CommandHandlerEnvironment)
}

typealias OnEnded = (TelegramSession) -> Unit


