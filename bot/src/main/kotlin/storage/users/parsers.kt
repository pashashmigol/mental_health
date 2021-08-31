package storage.users

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.parse
import lucher.LucherAnswersContainer
import lucher.LucherColor
import mmpi.MmpiAnswersContainer
import mmpi.MmpiProcess
import models.User
import quiz.DailyQuizAnswer
import quiz.DailyQuizAnswersContainer
import quiz.DailyQuizOptions


@Suppress("UNCHECKED_CAST")
 fun parseMmpiAnswers(snapshot: DataSnapshot?): List<MmpiAnswersContainer> {
    val typeIndicator: GenericTypeIndicator<HashMap<String, Any>> =
        object : GenericTypeIndicator<HashMap<String, Any>>() {}

    return snapshot?.getValue(typeIndicator)?.map { entry ->
        val answersMap: HashMap<String, Any> = entry.value as HashMap<String, Any>

        val user: User = answersMap["user"].let {
            it as HashMap<*, *>

            User(
                id = it["id"] as Long,
                name = it["name"] as String,
                googleDriveFolderId = it["googleDriveFolderId"] as String,
                googleDriveFolderUrl = it["googleDriveFolder"] as String,
                runDailyQuiz = it["runDailyQuiz"] as? Boolean ?: false
            )
        }
        val date: DateTimeTz = (answersMap["date"] as String).let {
            DateFormat.DEFAULT_FORMAT.parse(it)
        }
        val gender = (answersMap["gender"] as String).let {
            Gender.valueOf(it)
        }
        val answers = (answersMap["answersList"] as List<*>).map {
            MmpiProcess.Answer.valueOf(it as String)
        }
        MmpiAnswersContainer(
            user = user,
            date = date,
            gender = gender,
            answersList = answers
        )
    } ?: emptyList()
}

@Suppress("UNCHECKED_CAST")
 fun parseLucherAnswers(snapshot: DataSnapshot?): List<LucherAnswersContainer> {
    val typeIndicator: GenericTypeIndicator<HashMap<String, Any>> =
        object : GenericTypeIndicator<HashMap<String, Any>>() {}

    return snapshot?.getValue(typeIndicator)?.map { entry ->
        val answersMap: HashMap<String, Any> = entry.value as HashMap<String, Any>

        val user: User = answersMap["user"].let {
            it as HashMap<*, *>

            User(
                id = it["id"] as Long,
                name = it["name"] as String,
                googleDriveFolderId = it["googleDriveFolderId"] as String,
                googleDriveFolderUrl = it["googleDriveFolder"] as String,
                runDailyQuiz = it["runDailyQuiz"] as? Boolean ?: false
            )
        }
        val date: DateTimeTz = (answersMap["date"] as String).let {
            DateFormat.DEFAULT_FORMAT.parse(it)
        }
        val firstRound = (answersMap["firstRound"] as List<*>).map {
            LucherColor.valueOf(it as String)
        }
        val secondRound = (answersMap["secondRound"] as List<*>).map {
            LucherColor.valueOf(it as String)
        }
        LucherAnswersContainer(
            user = user,
            date = date,
            firstRound = firstRound,
            secondRound = secondRound
        )
    } ?: return emptyList()
}

@Suppress("UNCHECKED_CAST")
 fun parseDailyQuizAnswers(snapshot: DataSnapshot?): List<DailyQuizAnswersContainer> {
    val typeIndicator: GenericTypeIndicator<HashMap<String, Any>> =
        object : GenericTypeIndicator<HashMap<String, Any>>() {}

    return snapshot?.getValue(typeIndicator)?.map { entry ->
        val answersMap: HashMap<String, Any> = entry.value as HashMap<String, Any>

        val user: User = answersMap["user"].let {
            it as HashMap<*, *>

            User(
                id = it["id"] as Long,
                name = it["name"] as String,
                googleDriveFolderId = it["googleDriveFolderId"] as String,
                googleDriveFolderUrl = it["googleDriveFolderUrl"] as String,
                runDailyQuiz = it["runDailyQuiz"] as? Boolean ?: false
            )
        }
        val date: DateTimeTz = (answersMap["date"] as String).let {
            DateFormat.DEFAULT_FORMAT.parse(it)
        }
        val answers: List<DailyQuizAnswer> = (answersMap["answersList"] as List<*>).map {
            it as HashMap<*, *>

            DailyQuizAnswer.Option(
                questionIndex = (it["questionIndex"] as Long).toInt(),
                questionText = it["questionText"] as String,
                option = DailyQuizOptions.valueOf(it["option"] as String)
            )
        }
        DailyQuizAnswersContainer(
            user = user,
            date = date,
            answers = answers
        )
    } ?: emptyList()
}

 fun parseUsers(snapshot: DataSnapshot?): Map<Long, User> {
    return snapshot!!.children.associate {
        val user = it.getValue(User::class.java)
        Pair(user.id, user)
    }
}