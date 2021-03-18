package telegram.helpers

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import models.User
import storage.CentralDataStorage
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

fun CommandHandlerEnvironment.ourUser(): User? {
    val id = message.from?.id!!
    return CentralDataStorage.users.get(id)
}

fun CallbackQueryHandlerEnvironment.ourUser(): User? {
    val id = callbackQuery.from.id
    return CentralDataStorage.users.get(id)
}

fun showUsersList(
    bot: Bot,
    userId: Long,
) {
    val text = CentralDataStorage.users
        .allUsers()
        .joinToString("/n") { it.name }

    bot.sendMessage(
        chatId = userId,
        text = text
    )
}

