package lucher

import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import telegram.OnEnded
import telegram.TelegramSession

data class LucherSession(
    override val id: Long,
    val onEndedCallback: OnEnded
) : TelegramSession {

    private val replyOptions = mutableListOf(*options.map { it.callbackData() }.toTypedArray())
    var messages: Array<Message?> = arrayOfNulls(8)

    override fun start(env: CommandHandlerEnvironment) {
        replyOptions.clear()
        replyOptions.addAll(options.map { it.callbackData() })

        options.forEachIndexed { i, option ->
            val result = env.bot.sendPhoto(
                caption = option.caption,
                chatId = env.message.chat.id,
                photo = option.url,
            )
            messages[i] = result.first!!.body()!!.result!!
        }

        env.bot.sendMessage(
            chatId = env.message.chat.id,
            text = "Выберите наиболее приятный вам цвет",
            replyMarkup = InlineKeyboardMarkup.create(replyOptions)
        )
    }

    override fun callbackQuery(env: CallbackQueryHandlerEnvironment) {
        val answer = env.callbackQuery.data

        val user = "${env.callbackQuery.from.firstName} ${env.callbackQuery.from.lastName}"
        println("callbackQuery(); answer = $answer; user = $user")

        replyOptions.removeIf { it.callbackData == answer }

        env.bot.editMessageReplyMarkup(
            chatId = env.callbackQuery.message?.chat?.id,
            messageId = env.callbackQuery.message?.messageId,
            inlineMessageId = env.callbackQuery.inlineMessageId,
            replyMarkup = InlineKeyboardMarkup.create(replyOptions)
        )

        messages.find { it!!.caption == answer }
            ?.let {
                env.bot.deleteMessage(
                    chatId = env.callbackQuery.message?.chat?.id ?: 0,
                    messageId = it.messageId
                )
            }
    }
}

private data class Option(val caption: String, val url: String) {
    fun callbackData() = InlineKeyboardButton.CallbackData(text = caption, callbackData = caption)
}

private val options = arrayOf(
    Option("1", "https://drive.google.com/uc?export=download&id=1RJKBMtE7A1-serZ3yT-wFFmfKieSbURw"),
    Option("2", "https://drive.google.com/uc?export=download&id=1DHISDgiM6HPFWrDOnC09L1K9WGmTnpmX"),
    Option("3", "https://drive.google.com/uc?export=download&id=1QsRaeZ9KVI0GSQCF2AIGrXmUlL9sc1P0"),
    Option("4", "https://drive.google.com/uc?export=download&id=16HI01RELVjYcOyW9WBH46yP435-XshJu"),
    Option("5", "https://drive.google.com/uc?export=download&id=12pgfDxHfe3BMZwJelx0oA8PaHrrGEL5k"),
    Option("6", "https://drive.google.com/uc?export=download&id=1fmoDra7KpOukr8Pveu2RabQxE618AfwC"),
    Option("7", "https://drive.google.com/uc?export=download&id=1GuQ5B2jFD48rRVZcWjgB2VYymzZh9my1"),
    Option("8", "https://drive.google.com/uc?export=download&id=1FbQHlO_eycM9SVUOPkXJhEyjUzOKxEHF")
)


