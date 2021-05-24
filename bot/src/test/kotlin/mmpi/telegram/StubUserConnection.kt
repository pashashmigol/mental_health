package mmpi.telegram

import lucher.LucherColor
import telegram.Button
import telegram.UserConnection

class StubUserConnection : UserConnection {
    private val messageIds =  generateSequence(0L) { it + 1 }.iterator()

    override fun sendMessageWithButtons(
        chatId: Long,
        text: String,
        buttons: List<Button>,
        placeButtonsVertically: Boolean
    ): Long {
//        println("### sendMessageWithButtons(chatId: $chatId, text: $text)")
        return messageIds.next()
    }

    override fun sendMessage(chatId: Long, text: String, removeWhenSessionIsOver: Boolean) {
        println("### sendMessage(chatId: $chatId, text: $text)")
    }

    override fun updateMessage(chatId: Long, messageId: Long, text: String, buttons: List<Button>) {
        println("### updateMessage(chatId: $chatId, messageId: $messageId, text: $text, buttons: $buttons)")
    }

    override fun removeMessage(chatId: Long, messageId: Long) {
        println("### removeMessage(chatId: $chatId, messageId: $messageId)")
    }

    override fun setButtonsForMessage(
        chatId: Long,
        messageId: Long,
        buttons: MutableList<Button>,
        placeButtonsVertically: Boolean
    ) {
        println("### setButtonsForMessage(chatId: $chatId, messageId: $messageId, buttons: $buttons)")
    }

    override fun sendMessageWithLucherColor(chatId: Long, color: LucherColor) {
        println("### sendMessageWithPicture(chatId: $chatId, color: ${color.name})")
    }

    override fun cleanUp() {
        println("### cleanUp()")
    }

    override fun highlightAnswer(messageId: Long, answer: String) {
        println("### highlightAnswer(messageId: $messageId, answer: $answer)")
    }
}