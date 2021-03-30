package mmpi.telegram

import models.Question
import storage.CentralDataStorage.string
import telegram.Button
import telegram.UserConnection


fun askGender(
    userId: Long,
    question: Question,
    connection: UserConnection
): Long {
    return connection.sendMessageWithButtons(
        chatId = userId,
        text = string("choose_your_sex"),
        buttons = genderButtons(question)
    )
}

fun createGenderQuestion() = Question(
    text = string("choose_your_sex"),
    options = listOf(string("male"), string("female"))
)

fun mmpiButtons(question: Question): List<Button> {
    return question.options.mapIndexed { i, text ->
        Button(text = text, data = i.toString())
    }
}

fun genderButtons(question: Question): List<Button> {
    return question.options.mapIndexed { i: Int, s: String ->
        Button(text = s, data = i.toString())
    }
}