package mmpi

import models.Question
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

fun sendQuestion(
    bot: Bot,
    userId: Long,
    messageId: Long,
    question: Question
): Long {
    val result = bot.editMessageText(
        chatId = userId,
        messageId = messageId,
        text = question.text,
        replyMarkup = InlineKeyboardMarkup.create(mmpiButtons(question))
    )
    return result.first!!.body()!!.result!!.messageId
}

fun askGender(
    bot: Bot,
    userId: Long,
    question: Question
): Long {
    val result = bot.sendMessage(
        chatId = userId,
        text = question.text,
        replyMarkup = InlineKeyboardMarkup.create(genderButtons(question))
    )
    return result.first!!.body()!!.result!!.messageId
}

fun createGenderQuestion() = Question(
    text = "Выберите себе пол:",
    options = listOf("Мужской", "Женский")
)

fun mmpiButtons(question: Question): List<List<InlineKeyboardButton>> {
    return question.options.map {
        listOf(InlineKeyboardButton.CallbackData(text = it, callbackData = "0"))
    }
}

fun genderButtons(question: Question): List<List<InlineKeyboardButton>> {
    return listOf(question.options.mapIndexed { i: Int, s: String ->
        InlineKeyboardButton.CallbackData(text = s, callbackData = i.toString())
    })
}