package mmpi.telegram

import mmpi.MmpiProcess
import models.Question
import storage.CentralDataStorage.string
import telegram.Button
import telegram.QuizButton
import telegram.UserConnection
import telegram.UserId


fun askGender(
    userId: UserId,
    connection: UserConnection
): Long {
    return connection.sendMessageWithButtons(
        chatId = userId,
        text = string("choose_your_sex"),
        buttons = genderButtons()
    )
}

fun mmpiButtons(question: Question): List<Button> {
    return question.options.map {
        Button(
            text = it.text,
            quizButton = QuizButton.Mmpi(
                index = question.index,
                answer = MmpiProcess.Answer.valueOf(it.tag)
            )
        )
    }
}

fun genderButtons(): List<Button> {
    return Gender.values().map {
        Button(
            text = it.title,
            quizButton = QuizButton.GenderAnswer(
                answer = it
            )
        )
    }
}