package telegram

import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.PollAnswerHandlerEnvironment

data class LucherSession(
    override val id: Long,
    val callback: OnEnded
) : TelegramSession {
    override fun pollAnswer(env: PollAnswerHandlerEnvironment) {
        TODO("Not yet implemented")
    }

    override fun callbackQuery(env: CallbackQueryHandlerEnvironment) {
        TODO("Not yet implemented")
    }

    override fun start(env: CommandHandlerEnvironment) {
        TODO("Not yet implemented")
    }
}