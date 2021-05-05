package quiz

import models.Question
import storage.GoogleDriveConnection
import java.lang.IllegalStateException

fun loadDailyQuizData(connection: GoogleDriveConnection, fileId: String): DailyQuizData {
    return DailyQuizData(questions = reloadQuestions(connection, fileId))
}

private fun reloadQuestions(
    connection: GoogleDriveConnection,
    fileId: String
): List<Question> {

    val answerOptions: List<String> =
        connection.loadDataFromFile(
            fileId = fileId,
            page = "'answer_options'"
        ).dealWithError {
            throw IllegalStateException(it.message)
        }.map {
            it["answer"].toString()
        }

    val questions = connection.loadDataFromFile(
        fileId = fileId,
        page = "'questions'"
    ).dealWithError {
        throw IllegalStateException(it.message)
    }.mapIndexed { index, map ->
        map.toQuestion(index, answerOptions)
    }

    val size = questions.size
    return questions.mapIndexed { i: Int, question: Question ->
        question.copy(text = "(${i + 1}/$size) ${question.text}:")
    }
}

private fun Map<String, Any>.toQuestion(index: Int, answerOptions: List<String>): Question {
    return Question(
        index = index,
        text = stringFor("question"),
        options = answerOptions.mapIndexed { _, answer ->
            Question.Option(answer, answer)
        }
    )
}

private fun Map<String, Any>.stringFor(key: String) = (this[key] as? String) ?: ""