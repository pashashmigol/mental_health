package telegram

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
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
//    private val sessionState: SessionState,
    private val botKeeper: () -> BotsKeeper
) : UserConnection {

    //    private val sentMessages = mutableMapOf<Long, Message>()
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
                buttons.map {
                    listOf(InlineKeyboardButton.CallbackData(it.text, it.callback.makeString()))
                }
            } else {
                listOf(buttons.map {
                    InlineKeyboardButton.CallbackData(it.text, it.callback.makeString())
                })
            }

        val result = botKeeper().clientBot.sendMessage(
            chatId = chatId,
            text = text,
            replyMarkup = InlineKeyboardMarkup.create(options)
        )
        return result.first?.body()?.result?.messageId ?: -1L
    }

    override fun sendMessage(
        chatId: Long,
        text: String,
//        removeWhenSessionIsOver: Boolean
    ): MessageId {
        if (paused.get()) {
            return -1L
        }
        val result = botKeeper().clientBot.sendMessage(
            chatId = chatId,
            text = text
        )
//        if (removeWhenSessionIsOver) {
//            result.first?.body()?.result?.let { message ->
////                sentMessages[message.messageId] = message
//                sessionState.addMessageId(message.messageId)
//            }
//        }
        return result.first?.body()?.result?.messageId ?: -1L
    }

    override fun notifyAdmin(
        text: String,
        exception: Throwable?
    ): MessageId {
        val result = botKeeper().adminBot.notifyAdmin(
            adminId = adminId,
            message = text,
            exception = exception
        )
        return result.first?.body()?.result?.messageId ?: -1L
    }

    override fun cleanUp(chatId: ChatId, messageIds: List<MessageId>?) {
        messageIds?.forEach { messageId ->
            botKeeper().clientBot.deleteMessage(chatId, messageId)
        }
    }

    override fun removeMessage(chatId: Long, messageId: Long) {
        botKeeper().clientBot.deleteMessage(chatId, messageId)
    }

    override fun sendMessageWithLucherColor(
        chatId: Long,
        color: LucherColor
    ): MessageId {
        if (paused.get()) {
            return -1L
        }
        val result = botKeeper().clientBot.sendPhoto(
            caption = "${color.index} - ${color.name}",
            disableNotification = true,
            chatId = chatId,
            photo = color.url(),
        )
//        result.first?.body()?.result?.let { message: Message ->
////            sentMessages[message.messageId] = message
//            sessionState.addMessageId(message.messageId)
//        }

        return result.first?.body()?.result?.messageId ?: -1L
    }

    override fun sendMessagesWithLucherColors(
        chatId: Long,
        colors: Array<LucherColor>
    ): List<MessageId> {
        if (paused.get()) {
            return listOf()
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
//
//        result.first?.body()?.result?.let { messages: Array<Message> ->
////            sentMessages.putAll(messages.map { it.messageId to it })
//            sessionState.addMessageIds(messages.map { it.messageId })
//        }

        return result.first?.body()?.result?.map { it.messageId } ?: listOf()
    }

    override fun setButtonsForMessage(
        chatId: ChatId,
        messageId: MessageId?,
        buttons: MutableList<Button>,
        placeButtonsVertically: Boolean
    ): MessageId {
        if (paused.get()) {
            return -1L
        }
        val options =
            if (placeButtonsVertically) {
                buttons.map { listOf(InlineKeyboardButton.CallbackData(it.text, it.callback.makeString())) }
            } else {
                listOf(buttons.map { InlineKeyboardButton.CallbackData(it.text, it.callback.makeString()) })
            }

        val result = botKeeper().clientBot.editMessageReplyMarkup(
            chatId = chatId,
            messageId = messageId,
            replyMarkup = InlineKeyboardMarkup.create(options)
        )
        return result.first?.body()?.result?.messageId ?: -1L
    }

    override fun highlightAnswer(
        messageId: MessageId?,
        chatId: ChatId,
        buttons: List<Button>,
        buttonToHighLight: Int
    ): MessageId {
        if (paused.get()) {
            return -1L
        }
//        message?.let { mes ->
//        val buttons: List<List<InlineKeyboardButton.CallbackData>>? = mes.replyMarkup?.inlineKeyboard?.map {
//            it.map { button ->
//                (button as InlineKeyboardButton.CallbackData)
//                if (button.callbackData == answer.makeString()) {
//
//                    InlineKeyboardButton.CallbackData(
//                        text = button.text + " + ",
//                        callbackData = button.callbackData
//                    )
//                } else {
//                    button
//                }
//            }
//        }

        buttons
            .mapIndexed { i: Int, button: Button ->
                if (i == buttonToHighLight) button.copy(text = button.text + " + ") else button
            }
            .map { InlineKeyboardButton.CallbackData(it.text, it.callback.makeString()) }
            .map { listOf(it) }
            .let {
                val result = botKeeper().clientBot.editMessageReplyMarkup(
                    chatId = chatId,
                    messageId = messageId,
                    replyMarkup = InlineKeyboardMarkup.create(it)
                )
                return result.first?.body()?.result?.messageId ?: -1L
            }
    }

    override fun updateMessage(
        chatId: Long,
        messageId: Long,
        text: String,
        buttons: List<Button>
    ): MessageId {
        if (paused.get()) {
            return -1L
        }
        val markup = buttons.map {
            InlineKeyboardButton.CallbackData(it.text, it.callback.makeString())
        }
        val result = botKeeper().clientBot.editMessageText(
            chatId = chatId,
            messageId = messageId,
            text = text,
            replyMarkup = InlineKeyboardMarkup.create(markup)
        )
//        result.first?.body()?.result?.let { message ->
//            sentMessages[message.messageId] = message
//            sessionState.addMessageId(message.messageId)
//        }
        return result.first?.body()?.result?.messageId ?: -1L
    }

    override fun pause() {
        paused.set(true)
    }

    override fun resume() {
        paused.set(false)
    }
}