package telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import lucher.LucherColor
import lucher.url

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

class TelegramUserConnection(private val bot: ()->Bot) : UserConnection {

    private val sentMessages = mutableSetOf<Message>()

    override fun sendMessageWithButtons(
        chatId: Long,
        text: String,
        buttons: List<Button>,
        placeButtonsVertically: Boolean
    ): Long {
        val options =
            if (placeButtonsVertically) {
                listOf(buttons.map { InlineKeyboardButton.CallbackData(it.text, it.data) })
            } else {
                buttons.map { listOf(InlineKeyboardButton.CallbackData(it.text, it.data)) }
            }

        val result = bot().sendMessage(
            chatId = chatId,
            text = text,
            replyMarkup = InlineKeyboardMarkup.create(options)
        )
        result.first?.body()?.result?.let { message ->
            sentMessages.add(message)
            return message.messageId
        }
        return -1
    }

    override fun sendMessage(chatId: Long, text: String) {
        val result = bot().sendMessage(
            chatId = chatId,
            text = text
        )
        result.first?.body()?.result?.let { message ->
            sentMessages.add(message)
        }
    }

    override fun cleanUp() {
        sentMessages.forEach {
            bot().deleteMessage(it.chat.id, it.messageId)
        }
    }

    override fun removeMessage(chatId: Long, messageId: Long) {
        bot().deleteMessage(chatId, messageId)
    }

    override fun sendMessageWithPicture(
        chatId: Long,
        color: LucherColor
    ) {
        val result = bot().sendPhoto(
            caption = color.name,
            disableNotification = true,
            chatId = chatId,
            photo = color.url(),
        )
        result.first?.body()?.result?.let { message ->
            sentMessages.add(message)
        }
    }

    override fun setButtonsForMessage(chatId: Long, messageId: Long, options: MutableList<Button>) {
        val markup = options.map {
            InlineKeyboardButton.CallbackData(it.text, it.data)
        }
        bot().editMessageReplyMarkup(
            chatId = chatId,
            messageId = messageId,
            replyMarkup = InlineKeyboardMarkup.create(markup)
        )
    }

    override fun updateMessage(
        chatId: Long,
        messageId: Long,
        text: String,
        buttons: List<Button>
    ) {
        val markup = buttons.map {
            InlineKeyboardButton.CallbackData(it.text, it.data)
        }
        val result = bot().editMessageText(
            chatId = chatId,
            messageId = messageId,
            text = text,
            replyMarkup = InlineKeyboardMarkup.create(markup)
        )
        result.first?.body()?.result?.let { message ->
            sentMessages.add(message)
        }
    }
}
