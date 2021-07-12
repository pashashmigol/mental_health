package telegram.helpers

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import models.User
import org.apache.poi.util.StringUtil
import storage.CentralDataStorage
import storage.CentralDataStorage.string
import telegram.ChatInfo
import telegram.UserConnection

fun showResult(
    user: User,
    resultLink: String,
    userConnection: UserConnection
) {
    userConnection.sendMessage(
        chatId = user.id,
        text = string("your_results", resultLink)
    )
    userConnection.notifyAdmin(
        text = string("user_completed_test", user.name, resultLink)
    )
}

fun CommandHandlerEnvironment.chatInfo() = ChatInfo(
    userId = message.from!!.id,
    userName = message.from!!.run { formatName(firstName, lastName) },
    chatId = message.chat.id,
    messageId = message.messageId
)

fun formatName(
    firstName: String?,
    lastName: String?
): String {
    return StringUtil.join(" ", firstName ?: "", lastName ?: "").trim()
}

fun CallbackQueryHandlerEnvironment.chatInfo() = ChatInfo(
    userId = callbackQuery.from.id,
    userName = callbackQuery.from.firstName + " " + callbackQuery.from.lastName,
    chatId = callbackQuery.message!!.chat.id,
    messageId = callbackQuery.message!!.messageId
)

fun showUsersList(
    bot: Bot,
    userId: Long,
) {
    val text = CentralDataStorage.usersStorage
        .allUsers()
        .joinToString("/n") { "${it.name}: ${it.googleDriveFolder}" }

    bot.sendMessage(
        chatId = userId,
        text = text
    )
}

