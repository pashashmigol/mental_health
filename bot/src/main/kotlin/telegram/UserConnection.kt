package telegram

import lucher.LucherColor

interface UserConnection {
    fun sendMessageWithButtons(
        chatId: Long,
        text: String,
        buttons: List<Button>,
        placeButtonsVertically: Boolean = true
    ): MessageId = NOT_SENT

    fun sendMessage(
        chatId: Long,
        text: String,
    ): MessageId = NOT_SENT

    fun notifyAdmin(
        text: String,
        exception: Throwable? = null
    ): MessageId = NOT_SENT

    fun updateMessage(
        chatId: Long,
        messageId: Long,
        text: String,
        buttons: List<Button>
    ): MessageId = NOT_SENT

    fun removeMessage(chatId: Long, messageId: Long) {}

    fun setButtonsForMessage(
        chatId: ChatId,
        messageId: MessageId?,
        buttons: MutableList<Button>,
        placeButtonsVertically: Boolean = true
    ): MessageId = NOT_SENT

    fun sendMessageWithLucherColor(
        chatId: Long,
        color: LucherColor
    ): MessageId = NOT_SENT

    fun cleanUp(chatId: ChatId, messageIds: List<MessageId>?) {}

    fun highlightAnswer(
        messageId: MessageId?,
        chatId: ChatId,
        buttons: List<Button>,
        buttonToHighLight: Int
    ): MessageId = NOT_SENT

    fun sendMessagesWithLucherColors(
        chatId: Long,
        colors: Array<LucherColor>
    ): List<MessageId> = listOf()

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
    val userAnswer: UserAnswer
)