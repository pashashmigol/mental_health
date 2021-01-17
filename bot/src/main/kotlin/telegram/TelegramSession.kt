package telegram

import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.PollAnswerHandlerEnvironment

interface TelegramSession {
    val id: Long
    fun pollAnswer(env: PollAnswerHandlerEnvironment)
    fun callbackQuery(env: CallbackQueryHandlerEnvironment)
    fun start(env: CommandHandlerEnvironment)
}

typealias OnEnded = (TelegramSession) -> Unit


