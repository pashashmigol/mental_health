package telegram

import lucher.LucherColor
import java.lang.Exception

interface UserConnection {
    fun sendMessageWithButtons(
        chatId: Long,
        text: String,
        buttons: List<Button>,
        placeButtonsVertically: Boolean = true
    ): Long = 0

    fun sendMessage(
        chatId: Long,
        text: String,
        removeWhenSessionIsOver: Boolean = true
    ) {
    }

    fun notifyAdmin(
        text: String,
        exception: Throwable? = null
    ) {
    }

    fun updateMessage(
        chatId: Long,
        messageId: Long,
        text: String,
        buttons: List<Button>
    ) {
    }

    fun removeMessage(chatId: Long, messageId: Long) {}

    fun setButtonsForMessage(
        chatId: ChatId,
        messageId: MessageId?,
        buttons: MutableList<Button>,
        placeButtonsVertically: Boolean = true
    ) {
    }

    fun sendMessageWithLucherColor(
        chatId: Long,
        color: LucherColor
    ) {
    }

    fun cleanUp() {}

    fun highlightAnswer(
        messageId: MessageId?,
        answer: Callback
    ) {
    }

    fun sendMessageWithLucherColors(
        chatId: Long,
        colors: Array<LucherColor>
    ) {
    }

    fun pause() {}

    fun resume() {}
}

data class ChatInfo(
    val userId: Long,
    val userName: String,
    val chatId: Long,
    val messageId: Long
) {
    override fun toString(): String {
        return "ChatInfo(userId=$userId, userName='$userName', chatId=$chatId, messageId=$messageId)"
    }
}

data class Button(
    val text: String,
    val callback: Callback
)