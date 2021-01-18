package telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton

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


//
//internal fun createGenderQuestion() = Message.Question(
//    text = "Выберите себе пол:",
//    options = listOf("Мужской", "Женский")
//)
