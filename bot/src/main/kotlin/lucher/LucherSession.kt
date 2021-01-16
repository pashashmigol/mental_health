package lucher

import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.inputmedia.GroupableMedia
import com.github.kotlintelegrambot.entities.inputmedia.InputMediaPhoto
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

object LucherSession {
    private val mediaGroup = MediaGroup.from(*allOptions)
    private val replyOptions = mutableListOf(*allReplyOptions.toTypedArray())
    var messages: Array<Message>? = null

    fun startTest(environment: CommandHandlerEnvironment) {
        replyOptions.clear()
        replyOptions.addAll(allReplyOptions)

        val res = environment.bot.sendMediaGroup(
            chatId = environment.message.chat.id,
            disableNotification = true,
            mediaGroup = mediaGroup
        )

        messages = res.first?.body()?.result

        environment.bot.sendMessage(
            chatId = environment.message.chat.id,
            text = "Выберите наиболее приятный вам цвет",
            replyMarkup = InlineKeyboardMarkup.create(replyOptions)
        )
    }

    fun onCallBack(environment: CallbackQueryHandlerEnvironment) {
        val answer = environment.callbackQuery.data

        val user = "${environment.callbackQuery.from.firstName} ${environment.callbackQuery.from.lastName}"
        println("callbackQuery(); answer = $answer; user = $user")

        replyOptions.removeIf { it.callbackData == answer }

        environment.bot.editMessageReplyMarkup(
            chatId = environment.callbackQuery.message?.chat?.id,
            messageId = environment.callbackQuery.message?.messageId,
            inlineMessageId = environment.callbackQuery.inlineMessageId,
            replyMarkup = InlineKeyboardMarkup.create(replyOptions)
        )

        messages
            ?.find { it.caption == answer }
            ?.let {
                environment.bot.deleteMessage(
                    chatId = environment.callbackQuery.message?.chat?.id ?: 0,
                    messageId = it.messageId
                )
            }
    }
}

private val allReplyOptions = listOf(
    InlineKeyboardButton.CallbackData(text = "1", callbackData = "1"),
    InlineKeyboardButton.CallbackData(text = "2", callbackData = "2"),
    InlineKeyboardButton.CallbackData(text = "3", callbackData = "3"),
    InlineKeyboardButton.CallbackData(text = "4", callbackData = "4"),
    InlineKeyboardButton.CallbackData(text = "5", callbackData = "5"),
    InlineKeyboardButton.CallbackData(text = "6", callbackData = "6"),
    InlineKeyboardButton.CallbackData(text = "7", callbackData = "7"),
    InlineKeyboardButton.CallbackData(text = "8", callbackData = "8"),
)

private val allOptions = arrayOf<GroupableMedia>(
    InputMediaPhoto(
        media = TelegramFile.ByUrl("https://drive.google.com/uc?export=download&id=1RJKBMtE7A1-serZ3yT-wFFmfKieSbURw"),
        caption = "1"
    ),
    InputMediaPhoto(
        media = TelegramFile.ByUrl("https://drive.google.com/uc?export=download&id=1DHISDgiM6HPFWrDOnC09L1K9WGmTnpmX"),
        caption = "2"
    ),
    InputMediaPhoto(
        media = TelegramFile.ByUrl("https://drive.google.com/uc?export=download&id=1QsRaeZ9KVI0GSQCF2AIGrXmUlL9sc1P0"),
        caption = "3"
    ),
    InputMediaPhoto(
        media = TelegramFile.ByUrl("https://drive.google.com/uc?export=download&id=16HI01RELVjYcOyW9WBH46yP435-XshJu"),
        caption = "4"
    ),
    InputMediaPhoto(
        media = TelegramFile.ByUrl("https://drive.google.com/uc?export=download&id=12pgfDxHfe3BMZwJelx0oA8PaHrrGEL5k"),
        caption = "5"
    ),
    InputMediaPhoto(
        media = TelegramFile.ByUrl("https://drive.google.com/uc?export=download&id=1fmoDra7KpOukr8Pveu2RabQxE618AfwC"),
        caption = "6"
    ),
    InputMediaPhoto(
        media = TelegramFile.ByUrl("https://drive.google.com/uc?export=download&id=1GuQ5B2jFD48rRVZcWjgB2VYymzZh9my1"),
        caption = "7"
    ),
    InputMediaPhoto(
        media = TelegramFile.ByUrl("https://drive.google.com/uc?export=download&id=1FbQHlO_eycM9SVUOPkXJhEyjUzOKxEHF"),
        caption = "8"
    )
)



