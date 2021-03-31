package telegram

fun sendError(
    to: Long,
    message: String? = null,
    exception: Throwable? = null
) {
    val text = listOfNotNull(
        message,
        exception?.message,
        exception?.stackTrace?.contentToString()
    ).joinToString(separator = "\n\n")

    BotsKeeper.adminBot.sendMessage(
        chatId = to,
        text = text
    )
//    BotsKeeper.clientBot.sendMessage(
//        chatId = userId,
//        text = text
//    )
}