package telegram

fun sendError(
    userId: Long,
    message: String? = null,
    exception: Throwable? = null
) {
    val text = message + "\n\n" + exception?.message +
            "\n\n" + exception?.stackTrace.contentToString()

    BotsKeeper.adminBot.sendMessage(
        chatId = userId,
        text = text
    )
//    BotsKeeper.clientBot.sendMessage(
//        chatId = userId,
//        text = text
//    )
}