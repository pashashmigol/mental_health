package telegram

import com.github.kotlintelegrambot.Bot

fun sendError(
    bot: Bot,
    userId: Long,
    message: String? = null,
    exception: Throwable? = null
) {
    bot.sendMessage(
        chatId = userId,
        text = message + "\n\n" + exception?.message +
                "\n\n" + exception?.stackTrace.contentToString()
    )
}