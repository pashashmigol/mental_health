package telegram.helpers

import com.github.kotlintelegrambot.Bot

fun showResult(
    bot: Bot,
    userId: Long,
    link: String
) {
    bot.sendMessage(
        chatId = userId,
        text = "Ваши результаты: $link"
    )
}