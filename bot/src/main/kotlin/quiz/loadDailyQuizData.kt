package quiz

import models.Question
import storage.GoogleDriveConnection
import java.lang.IllegalStateException

fun loadDailyQuizData(connection: GoogleDriveConnection, fileId: String): DailyQuizData {
    return DailyQuizData(
        morningQuestionsClosed = loadClosedQuestions(connection, fileId, "morning_questions_closed"),
        eveningQuestionsClosed = loadClosedQuestions(connection, fileId, "evening_questions_closed"),
        morningQuestionsOpen = loadOpenQuestions(connection, fileId, "morning_questions_open"),
        eveningQuestionsOpen = loadOpenQuestions(connection, fileId, "evening_questions_open")
    )
}

private fun loadOpenQuestions(
    connection: GoogleDriveConnection,
    fileId: String,
    columnId: String
): List<Question> {
    val questions = connection.loadDataFromFile(
        fileId = fileId,
        page = "'questions'"
    ).dealWithError {
        throw IllegalStateException(it.message)
    }.mapIndexed { index, map ->
        map.toQuestion(index, emptyList(), columnId)
    }.filterNotNull()

    val size = questions.size
    return questions.mapIndexed { i: Int, question: Question ->
        question.copy(text = "(${i + 1}/$size) ${question.text}")
    }
}

private fun loadClosedQuestions(
    connection: GoogleDriveConnection,
    fileId: String,
    columnId: String
): List<Question> {
    val options = connection.loadDataFromFile(
        fileId = fileId,
        page = "'answer_options'"
    ).dealWithError {
        throw IllegalStateException(it.message)
    }.map {
        it["answer"].toString()
    }

    val answerOptions: List<Question.Option> = options.zip(
        other = DailyQuizOptions.values(),
        transform = { text: String, option: DailyQuizOptions ->
            Question.Option(text = text, tag = option.name)
        }
    )

    val questions = connection.loadDataFromFile(
        fileId = fileId,
        page = "'questions'"
    ).dealWithError {
        throw IllegalStateException(it.message)
    }.mapIndexed { index, map ->
        map.toQuestion(index, answerOptions, columnId)
    }.filterNotNull()

    val size = questions.size
    return questions.mapIndexed { i: Int, question: Question ->
        question.copy(text = "(${i + 1}/$size) ${question.text}")
    }
}

private fun Map<String, Any>.toQuestion(
    index: Int,
    answerOptions: List<Question.Option>,
    columnId: String
): Question? {
    return stringFor(columnId)?.let {
        Question(
            index = index,
            text = it,
            options = answerOptions
        )
    }
}

private fun Map<String, Any>.stringFor(key: String) = (this[key] as? String)