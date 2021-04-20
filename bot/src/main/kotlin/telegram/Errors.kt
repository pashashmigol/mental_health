package telegram

import Settings
import com.github.kotlintelegrambot.Bot


fun Bot.notifyAdmin(
    adminId: Long,
    message: String? = null,
    exception: Throwable? = null
) {
    val text = listOfNotNull(
        message,
        exception?.message,
        exception?.stackTrace?.contentToString()
    ).joinToString(separator = "\n\n")

    sendMessage(
        chatId = adminId,
        text = text
    )
//    BotsKeeper.clientBot.sendMessage(
//        chatId = userId,
//        text = text
//    )
}