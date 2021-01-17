package telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

internal fun sendResult(
    bot: Bot,
    userId: Long,
    result: Message.TestResult
) {
    bot.sendMessage(
        chatId = userId,
        text = result.text
    )
}

internal fun sendQuestion(
    bot: Bot,
    userId: Long,
    question: Message.Question
): Long {
    val result = bot.sendMessage(
        chatId = userId,
        text = question.text,
        replyMarkup = InlineKeyboardMarkup.create(replyOptions(question))
    )
    return result.first!!.body()!!.result!!.messageId
}

internal fun createGenderQuestion() = Message.Question(
    text = "Выберите себе пол:",
    options = listOf("Мужской", "Женский")
)

internal fun replyOptions(question: Message.Question): List<InlineKeyboardButton> {
    return question.options.mapIndexed { index, option ->
        InlineKeyboardButton.CallbackData(text = option, callbackData = index.toString())
    }
}