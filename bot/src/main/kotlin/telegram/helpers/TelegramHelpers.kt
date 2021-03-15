package telegram.helpers

import com.github.kotlintelegrambot.Bot
import storage.CentralDataStorage.string

fun showResult(
    bot: Bot,
    userId: Long,
    link: String
) {
    bot.sendMessage(
        chatId = userId,
        text = string("results", link)
    )
}