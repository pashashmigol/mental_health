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
        buttons = buttons(question)
    )
}

fun createGenderQuestion() = Question(
    index = -1,
    text = string("choose_your_sex"),
    options = listOf(
        Question.Option(text = Gender.Male.title, tag = Gender.Male.name),
        Question.Option(text = Gender.Female.title, tag = Gender.Female.name)
    )
)

fun buttons(question: Question): List<Button> {
    return question.options.map {
        Button(text = it.text, data = it.tag)
    }
}