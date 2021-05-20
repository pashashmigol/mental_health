package telegram

import com.github.kotlintelegrambot.Bot


fun Bot.notifyAdmin(
    adminId: Long,
    message: String? = null,
    exception: Throwable? = null
) = sendMessage(
    chatId = adminId,
    text = formatMessage(message, exception)
)

internal fun formatMessage(message: String?, exception: Throwable?): String {
    return listOfNotNull(
        message,
        exception?.message,
        exception?.stackTrace?.contentToString()
    ).joinToString(separator = "\n\n")
}


