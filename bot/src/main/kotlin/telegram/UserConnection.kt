package telegram

import lucher.LucherColor

interface UserConnection {
    fun sendMessageWithButtons(
        chatId: Long,
        text: String,
        buttons: List<Button>,
        placeButtonsVertically: Boolean = false
    ): Long

    fun sendMessage(
        chatId: Long,
        text: String
    )

    fun updateMessage(
        chatId: Long,
        messageId: Long,
        text: String,
        buttons: List<Button>
    )

    fun removeMessage(chatId: Long, messageId: Long)

    fun setButtonsForMessage(
        chatId: Long,
        messageId: Long,
        options: MutableList<Button>
    )

    fun sendMessageWithPicture(
        chatId: Long,
        color: LucherColor
    )

    fun cleanUp()

    fun highlightAnswer(messageId: Long, answer: String)
}

data class ChatInfo(
    val userId: Long,
    val userName: String,
    val chatId: Long,
    val messageId: Long
){
    override fun toString(): String {
        return "ChatInfo(userId=$userId, userName='$userName', chatId=$chatId, messageId=$messageId)"
    }
}

data class Button(
    val text: String,
    val data: String
)