package telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.inputmedia.GroupableMedia
import com.github.kotlintelegrambot.entities.inputmedia.InputMediaPhoto
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import lucher.LucherColor
import lucher.url
import java.lang.Exception

class TelegramUserConnection(private val adminId: Long, private val bot: () -> Bot) : UserConnection {

    private val sentMessages = mutableMapOf<Long, Message>()

    override fun sendMessageWithButtons(
        chatId: Long,
        text: String,
        buttons: List<Button>,
        placeButtonsVertically: Boolean
    ): Long {
        val options =
            if (placeButtonsVertically) {
                buttons.map { listOf(InlineKeyboardButton.CallbackData(it.text, it.data)) }
            } else {
                listOf(buttons.map { InlineKeyboardButton.CallbackData(it.text, it.data) })
            }

        val result = bot().sendMessage(
            chatId = chatId,
            text = text,
            replyMarkup = InlineKeyboardMarkup.create(options)
        )
        result.first?.body()?.result?.let { message ->
            sentMessages[message.messageId] = message
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
            sentMessages[message.messageId] = message
        }
    }

    override fun notifyAdmin(
        text: String,
        exception: Throwable?
    ) {
        bot().notifyAdmin(
            adminId = adminId,
            message = text,
            exception = exception
        )
    }

    override fun cleanUp() {
        sentMessages.forEach {
            bot().deleteMessage(it.value.chat.id, it.value.messageId)
        }
    }

    override fun removeMessage(chatId: Long, messageId: Long) {
        bot().deleteMessage(chatId, messageId)
    }

    override fun sendMessageWithLucherColor(
        chatId: Long,
        color: LucherColor
    ) {
        val result = bot().sendPhoto(
            caption = "${color.index} - ${color.name}",
            disableNotification = true,
            chatId = chatId,
            photo = color.url(),
        )
        result.first?.body()?.result?.let { message ->
            sentMessages[message.messageId] = message
        }
    }

    override fun sendMessageWithLucherColors(
        chatId: Long,
        colors: Array<LucherColor>
    ) {
        val allOptions = colors.map { color ->
            InputMediaPhoto(
                caption = "${color.index} - ${color.name}",
                media = TelegramFile.ByUrl(color.url()),
            )
        }.toTypedArray<GroupableMedia>()

        val mediaGroup = MediaGroup.from(*allOptions)

        val result = bot().sendMediaGroup(
            chatId = chatId,
            disableNotification = true,
            mediaGroup = mediaGroup,
        )

        result.first?.body()?.result?.let { messages ->
            sentMessages.putAll(messages.map { it.messageId to it })
        }
    }

    override fun setButtonsForMessage(
        chatId: Long,
        messageId: Long,
        buttons: MutableList<Button>,
        placeButtonsVertically: Boolean
    ) {
        val options =
            if (placeButtonsVertically) {
                buttons.map { listOf(InlineKeyboardButton.CallbackData(it.text, it.data)) }
            } else {
                listOf(buttons.map { InlineKeyboardButton.CallbackData(it.text, it.data) })
            }

        bot().editMessageReplyMarkup(
            chatId = chatId,
            messageId = messageId,
            replyMarkup = InlineKeyboardMarkup.create(options)
        )
    }

    override fun highlightAnswer(messageId: Long, answer: String) {
        val message = sentMessages[messageId]

        message?.let { mes ->
            val buttons = mes.replyMarkup?.inlineKeyboard?.map {
                it.map { button ->
                    (button as InlineKeyboardButton.CallbackData)
                    if (button.callbackData == answer) {

                        InlineKeyboardButton.CallbackData(
                            text = button.text + " + ",
                            callbackData = button.callbackData
                        )
                    } else {
                        button
                    }
                }
            }
            buttons?.let {
                bot().editMessageReplyMarkup(
                    chatId = mes.chat.id,
                    messageId = messageId,
                    replyMarkup = InlineKeyboardMarkup.create(buttons)
                )
            }
        }
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
            sentMessages[message.messageId] = message
        }
    }
}