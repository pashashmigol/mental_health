package telegram.helpers

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import models.User
import storage.CentralDataStorage
import storage.CentralDataStorage.string

fun showResult(
    user: User,
    adminId: Long,
    resultLink: String,
    adminBot: Bot,
    clientBot: Bot
) {
    clientBot.sendMessage(
        chatId = user.id,
        text = string("your_results", resultLink)
    )
    adminBot.sendMessage(
        chatId = adminId,
        text = string("user_completed_test", user.name, resultLink)
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
        .joinToString("/n") { "${it.name}: ${it.googleDriveFolder}" }

    bot.sendMessage(
        chatId = userId,
        text = text
    )
}

