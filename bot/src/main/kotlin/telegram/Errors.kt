package telegram

import Settings


fun notifyAdmin(
    message: String? = null,
    exception: Throwable? = null
) {
    val text = listOfNotNull(
        message,
        exception?.message,
        exception?.stackTrace?.contentToString()
    ).joinToString(separator = "\n\n")

    BotsKeeper.adminBot.sendMessage(
        chatId = Settings.ADMIN_ID,
        text = text
    )
//    BotsKeeper.clientBot.sendMessage(
//        chatId = userId,
//        text = text
//    )
}