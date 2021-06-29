package telegram

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.inputmedia.GroupableMedia
import com.github.kotlintelegrambot.entities.inputmedia.InputMediaPhoto
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import io.ktor.util.*
import lucher.LucherColor
import lucher.url
import java.util.concurrent.atomic.AtomicBoolean

@InternalAPI
class TelegramUserConnection(
    private val adminId: Long,
    private val botKeeper: () -> BotsKeeper
) : UserConnection {

    private val sentMessages = mutableMapOf<Long, Message>()
    private val paused = AtomicBoolean(false)

    override fun sendMessageWithButtons(
        chatId: Long,
        text: String,
        buttons: List<Button>,
        placeButtonsVertically: Boolean
    ): Long {
        if (paused.get()) {
            return -1L
        }

        val options =
            if (placeButtonsVertically) {
                buttons.map { listOf(InlineKeyboardButton.CallbackData(it.text, it.data)) }
            } else {
                listOf(buttons.map { InlineKeyboardButton.CallbackData(it.text, it.data) })
            }

        val result = botKeeper().clientBot.sendMessage(
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

    override fun sendMessage(
        chatId: Long,
        text: String,
        removeWhenSessionIsOver: Boolean
    ) {
        if (paused.get()) {
            return
        }
        val result = botKeeper().clientBot.sendMessage(
            chatId = chatId,
            text = text
        )
        if (removeWhenSessionIsOver) {
            result.first?.body()?.result?.let { message ->
                sentMessages[message.messageId] = message
            }
        }
    }

    override fun notifyAdmin(
        text: String,
        exception: Throwable?
    ) {
        botKeeper().adminBot.notifyAdmin(
            adminId = adminId,
            message = text,
            exception = exception
        )
    }

    override fun cleanUp() {
        sentMessages.forEach {
            botKeeper().clientBot.deleteMessage(it.value.chat.id, it.value.messageId)
        }
    }

    override fun removeMessage(chatId: Long, messageId: Long) {
        botKeeper().clientBot.deleteMessage(chatId, messageId)
    }

    override fun sendMessageWithLucherColor(
        chatId: Long,
        color: LucherColor
    ) {
        if (paused.get()) {
            return
        }
        val result = botKeeper().clientBot.sendPhoto(
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
        if (paused.get()) {
            return
        }
        val allOptions = colors.map { color ->
            InputMediaPhoto(
                caption = "${color.index} - ${color.name}",
                media = TelegramFile.ByUrl(color.url()),
            )
        }.toTypedArray<GroupableMedia>()

        val mediaGroup = MediaGroup.from(*allOptions)

        val result = botKeeper().clientBot.sendMediaGroup(
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
        if (paused.get()) {
            return
        }
        val options =
            if (placeButtonsVertically) {
                buttons.map { listOf(InlineKeyboardButton.CallbackData(it.text, it.data)) }
            } else {
                listOf(buttons.map { InlineKeyboardButton.CallbackData(it.text, it.data) })
            }

        botKeeper().clientBot.editMessageReplyMarkup(
            chatId = chatId,
            messageId = messageId,
            replyMarkup = InlineKeyboardMarkup.create(options)
        )
    }

    override fun highlightAnswer(messageId: Long, answer: String) {
        if (paused.get()) {
            return
        }
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
                botKeeper().clientBot.editMessageReplyMarkup(
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
        if (paused.get()) {
            return
        }
        val markup = buttons.map {
            InlineKeyboardButton.CallbackData(it.text, it.data)
        }
        val result = botKeeper().clientBot.editMessageText(
            chatId = chatId,
            messageId = messageId,
            text = text,
            replyMarkup = InlineKeyboardMarkup.create(markup)
        )
        result.first?.body()?.result?.let { message ->
            sentMessages[message.messageId] = message
        }
    }

    override fun pause() {
        paused.set(true)
    }

    override fun resume() {
        paused.set(false)
    }
}