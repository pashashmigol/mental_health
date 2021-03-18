package lucher.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import kotlinx.coroutines.delay
import lucher.LucherColor
import lucher.callbackData
import lucher.url
import storage.CentralDataStorage.string


suspend fun askUserToWaitBeforeSecondRound(chatId: Long, bot: Bot, minutes: Int) {
    val waitingMessageId = askUserToWait(chatId, bot, minutes)
    delay(minutes * 60 * 1000L)
    removeMessage(chatId, bot, waitingMessageId)
}

fun askUserToWait(chatId: Long, bot: Bot, minutes: Int): Long {
    return bot.sendMessage(
        chatId = chatId,
        text = string("lucher_timeout", minutes)
    ).first!!.body()!!.result!!.messageId
}

fun removeMessage(chatId: Long, bot: Bot, messageId: Long) {
    bot.deleteMessage(
        chatId = chatId,
        messageId = messageId
    )
}

fun allColorsChosen(answers: List<String>) = answers.size == LucherColor.values().size - 1

fun askUserToChooseColor(
    chatId: Long,
    bot: Bot,
    options: List<InlineKeyboardButton>
) {
    bot.sendMessage(
        chatId = chatId,
        text = string("choose_color"),
        replyMarkup = InlineKeyboardMarkup.create(options)
    ).first!!.body()!!.result!!.messageId
}

fun showAllColors(chatId: Long, bot: Bot): Array<Message?> {
    val shownColors: Array<Message?> = arrayOfNulls(LucherColor.values().size)

    LucherColor.values().forEachIndexed { i, option ->
        val result = bot.sendPhoto(
            caption = option.index.toString(),
            disableNotification = true,
            chatId = chatId,
            photo = option.url(),
        )
        shownColors[i] = result.first!!.body()!!.result!!
    }
    return shownColors
}

fun cleanUp(
    env: CallbackQueryHandlerEnvironment,
    currentlyShownColors: Array<Message?>,
    options: List<InlineKeyboardButton>
) {
    currentlyShownColors.find { it!!.caption == options.first().text }?.let {
        env.bot.deleteMessage(
            chatId = env.callbackQuery.message?.chat?.id ?: 0,
            messageId = it.messageId
        )
    }
    env.callbackQuery.message!!.apply {
        env.bot.deleteMessage(this.chat.id, this.messageId)
    }
}

fun createReplyOptions(): MutableList<InlineKeyboardButton.CallbackData> {
    val options = mutableListOf<InlineKeyboardButton.CallbackData>()
    options.addAll(LucherColor.values().map { it.callbackData() })
    return options
}


fun removeChosenColor(
    env: CallbackQueryHandlerEnvironment,
    answer: String,
    currentlyShownColors: Array<Message?>
) {
    currentlyShownColors.find { it!!.caption == answer }?.let {
        env.bot.deleteMessage(
            chatId = env.callbackQuery.message?.chat?.id ?: 0,
            messageId = it.messageId
        )
    }
}

fun removePressedButton(
    answer: String,
    env: CallbackQueryHandlerEnvironment,
    options: MutableList<InlineKeyboardButton.CallbackData>
) {
    options.removeIf { it.callbackData == answer }

    env.bot.editMessageReplyMarkup(
        chatId = env.callbackQuery.message?.chat?.id,
        messageId = env.callbackQuery.message?.messageId,
        inlineMessageId = env.callbackQuery.inlineMessageId,
        replyMarkup = InlineKeyboardMarkup.create(options)
    )
}