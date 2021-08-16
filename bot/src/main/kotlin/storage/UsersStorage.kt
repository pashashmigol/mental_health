package storage

import Gender
import Result
import com.google.firebase.database.*
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.parse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import lucher.LucherAnswersContainer
import lucher.LucherColor
import mmpi.MmpiAnswersContainer
import mmpi.MmpiProcess
import models.AnswersContainer
import models.TypeOfTest
import models.User
import quiz.DailyQuizAnswer
import quiz.DailyQuizAnswersContainer
import quiz.DailyQuizOptions
import telegram.*
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap


private const val USERS = "users_info"
private const val MMPI_ANSWERS = "mmpi_answers"
private const val LUCHER_ANSWERS = "lucher_answers"
private const val DAILY_QUIZ_ANSWERS = "daily_quiz_answers"
private const val ACTIVE_SESSIONS = "active_sessions"

class UsersStorage(database: FirebaseDatabase) {

    private val usersInfoRef: DatabaseReference = database.reference.child(USERS)
    private val mmpiAnswersRef: DatabaseReference = database.reference.child(MMPI_ANSWERS)
    private val lucherAnswersRef: DatabaseReference = database.reference.child(LUCHER_ANSWERS)
    private val activeSessionsRef: DatabaseReference = database.reference.child(ACTIVE_SESSIONS)
    private val dailyQuizAnswersRef: DatabaseReference = database.reference.child(DAILY_QUIZ_ANSWERS)

    @Volatile
    private var users = ConcurrentHashMap<Long, User>()

    init {
        usersInfoRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                users.clear()
                users.putAll(parseUsers(snapshot))

                println("onDataChange(): $users")
            }

            override fun onCancelled(error: DatabaseError?) {
                println("onCancelled(): $error")
            }
        })
    }

    suspend fun saveDailyQuizAnswers(
        user: User,
        answers: DailyQuizAnswersContainer
    ): Result<Unit> {
        val dateString = answers.date.toString(DateFormat.DEFAULT_FORMAT)

        val hashMap = HashMap<String, Any>().apply {
            put("user", user)
            put("date", dateString)
            put("answersList", answers.answers)
        }
        val resultChannel = Channel<Result<Unit>>(1)

        dailyQuizAnswersRef
            .child(user.id.toString())
            .child(answers.dateString)
            .setValue(hashMap) { error: DatabaseError?, _: DatabaseReference ->
                println("saveDailyQuizAnswers(); userId ${user.id}, error: $error")
                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
        return resultChannel.receive()
    }

    suspend fun addSession(
        session: SessionState
    ): Result<Unit> {
        val resultChannel = Channel<Result<Unit>>(1)

        activeSessionsRef
            .child(session.sessionId.toString())
            .setValue(session) { error: DatabaseError?, _: DatabaseReference ->
                println("addSession(); session $session, error: $error")
                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
        return resultChannel.receive()
    }

    suspend fun addAnswer(
        sessionId: SessionId,
        userAnswer: UserAnswer,
        index: Int
    ): Result<Unit> {
        val resultChannel = Channel<Result<Unit>>(1)

        activeSessionsRef
            .child(sessionId.toString())
            .child("answers")
            .child(index.toString())
            .setValue(userAnswer) { error: DatabaseError?, _: DatabaseReference ->
                println("addAnswer(); callback $userAnswer")

                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
        return resultChannel.receive()
    }

    suspend fun addMessageId(
        sessionId: SessionId,
        messageId: MessageId,
        index: Int
    ): Result<Unit> {
        val resultChannel = Channel<Result<Unit>>(1)

        activeSessionsRef
            .child(sessionId.toString())
            .child("messageIds")
            .child(index.toString())
            .setValue(messageId) { error: DatabaseError?, _: DatabaseReference ->
                println("addMessageId($messageId);")

                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
        return resultChannel.receive()
    }

    suspend fun takeAllSessions(): Result<List<SessionState>> {
        val resultChannel = Channel<Result<List<SessionState>>>(1)

        activeSessionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                println("getUserAnswers():onDataChange(): count = ${snapshot?.childrenCount}")
                try {
                    val sessions = parseSessions(snapshot)
                    resultChannel.offer(Result.Success(sessions))
                } catch (e: Exception) {
                    resultChannel.offer(Result.Error(message = "takeAllSessions()", exception = e))
                } finally {
                    activeSessionsRef.removeEventListener(this)
                }
            }

            override fun onCancelled(error: DatabaseError?) {
                println("getUserAnswers().onCancelled(): $error")
                activeSessionsRef.removeEventListener(this)
                resultChannel.close()
            }
        })
        return resultChannel.receive()
    }

    fun clear() {
        val resultChannel = Channel<Result<Unit>>(5)
        activeSessionsRef
            .removeValue { error, _ ->
                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
        mmpiAnswersRef
            .removeValue { error, _ ->
                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
        lucherAnswersRef
            .removeValue { error, _ ->
                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
        dailyQuizAnswersRef
            .removeValue { error, _ ->
                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
        usersInfoRef
            .removeValue { error, _ ->
                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
    }

    fun removeSession(sessionId: SessionId): Result<Unit> = runBlocking {
        val resultChannel = Channel<Result<Unit>>(1)
        activeSessionsRef
            .child(sessionId.toString())
            .removeValue { error, ref ->
                println("removeSession($sessionId) ref: $ref, error: $error")

                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
        return@runBlocking resultChannel.receive()
    }

    fun getUser(userId: Long) = users[userId]

    fun allUsers(): List<User> = users.values.toList()

    fun hasUserWithId(userId: Long) = users.containsKey(userId)

    suspend fun saveUser(user: User): Result<Unit> {
        val resultChannel = Channel<Result<Unit>>(1)
        users[user.id] = user

        usersInfoRef
            .child(user.id.toString())
            .setValue(user) { error: DatabaseError?, ref: DatabaseReference ->
                println("add($user) ref: $ref, error: $error")

                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }

        return resultChannel.receive()
    }

    suspend fun saveMmpiAnswers(answers: MmpiAnswersContainer): Result<Unit> {
        val resultChannel = Channel<Result<Unit>>(1)

        val hashMap = HashMap<String, Any>().apply {
            put("date", answers.dateString)
            put("gender", answers.gender.name)

            put("user", HashMap<String, Any>().apply {
                put("id", answers.user.id)
                put("name", answers.user.name)
                put("googleDriveFolder", answers.user.googleDriveFolderUrl)
                put("googleDriveFolderId", answers.user.googleDriveFolderId)
            })
            put("answersList", answers.answersList.map { it.name })
        }

        mmpiAnswersRef
            .child(answers.user.id.toString())
            .child(answers.dateString)
            .setValue(hashMap) { error, ref ->
                println("add(${answers.user}) ref: $ref, error: $error")

                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
        return resultChannel.receive()
    }

    suspend fun saveLucherAnswers(answers: LucherAnswersContainer): Result<Unit> {
        val resultChannel = Channel<Result<Unit>>(1)

        val hashMap = HashMap<String, Any>().apply {
            put("date", answers.dateString)

            put("user", HashMap<String, Any>().apply {
                put("id", answers.user.id)
                put("name", answers.user.name)
                put("googleDriveFolder", answers.user.googleDriveFolderUrl)
                put("googleDriveFolderId", answers.user.googleDriveFolderId)
            })
            put("firstRound", answers.firstRound.map { it.name })
            put("secondRound", answers.secondRound.map { it.name })
        }
        lucherAnswersRef
            .child(answers.user.id.toString())
            .child(answers.dateString)
            .setValue(hashMap) { error, ref ->
                println("add(${answers.user}) ref: $ref, error: $error")

                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
        return resultChannel.receive()
    }

    suspend fun clearUser(user: User): Result<Unit> {
        val resultChannel = Channel<Unit>(3)
        mmpiAnswersRef
            .child(user.id.toString())
            .setValue(null) { error, ref ->
                println("add($user) ref: $ref, error: $error")
                resultChannel.offer(Unit)
            }
        lucherAnswersRef
            .child(user.id.toString())
            .setValue(null) { error, ref ->
                println("add($user) ref: $ref, error: $error")
                resultChannel.offer(Unit)
            }
        activeSessionsRef
            .child(user.id.toString())
            .setValue(null) { error, ref ->
                println("add($user) ref: $ref, error: $error")
                resultChannel.offer(Unit)
            }

        resultChannel.receive()
        return Result.Success(Unit)
    }

    suspend fun getUserAnswers(user: User): Result<List<AnswersContainer>> {
        val resultChannel = Channel<List<AnswersContainer>>(3)

        mmpiAnswersRef
            .child(user.id.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot?) {
                    println("mmpiAnswersRef.getUserAnswers():onDataChange(): count = ${snapshot?.childrenCount}")
                    try {
                        resultChannel.offer(parseMmpiAnswers(snapshot))
                    } catch (e: Exception) {
                        resultChannel.close(e)
                    }
                }

                override fun onCancelled(error: DatabaseError?) {
                    println("getUserAnswers():onCancelled(): $error")
                    resultChannel.close()
                }
            })

        lucherAnswersRef
            .child(user.id.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot?) {
                    println("usersLucherAnswersRef.getUserAnswers():onDataChange(): count = ${snapshot?.childrenCount}")
                    try {
                        resultChannel.offer(parseLucherAnswers(snapshot))
                    } catch (e: Exception) {
                        resultChannel.close(e)
                    }
                }

                override fun onCancelled(error: DatabaseError?) {
                    println("getUserAnswers():onCancelled(): $error")
                    resultChannel.close()
                }
            })

        dailyQuizAnswersRef
            .child(user.id.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot?) {
                    println("dailyQuizAnswersRef.getUserAnswers():onDataChange(): count = ${snapshot?.childrenCount}")
                    try {
                        resultChannel.offer(parseDailyQuizAnswers(snapshot))
                    } catch (e: Exception) {
                        resultChannel.close(e)
                    }
                }

                override fun onCancelled(error: DatabaseError?) {
                    println("getUserAnswers():onCancelled(): $error")
                    resultChannel.close()
                }
            })

        return try {
            val resultList = resultChannel.receive() + resultChannel.receive() + resultChannel.receive()
            Result.Success(resultList)
        } catch (e: ClosedReceiveChannelException) {
            println("getUserAnswers(): ClosedReceiveChannelException: ${e.message}")
            Result.Error("Get user $user answers failed with error")
        } catch (e: CancellationException) {
            println("getUserAnswers(): CancellationException: ${e.message}")
            Result.Error("Get user $user answers was cancelled")
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun parseSessions(snapshot: DataSnapshot?): List<SessionState> {

    val typeIndicator: GenericTypeIndicator<HashMap<String, Any>> =
        object : GenericTypeIndicator<HashMap<String, Any>>() {}

    return snapshot?.children?.map { dataSnapshot ->
        val sessionMap = dataSnapshot.getValue(typeIndicator)

        val sessionState = SessionState(
            userId = sessionMap["userId"] as UserId,
            roomId = sessionMap["roomId"] as RoomId,
            chatId = sessionMap["chatId"] as ChatId,
            sessionId = sessionMap["sessionId"] as SessionId,
            type = TypeOfTest.valueOf(sessionMap["type"] as String)
        )

        (sessionMap["messageIds"] as? ArrayList<Long>)
            ?.apply { sessionState.addMessageIds(this) }

        (sessionMap["answers"] as? ArrayList<HashMap<String, Any>>)
            ?.forEach {
                val type = UserAnswer.Type.valueOf(it["type"] as String)
                val index = (it["index"] as? Long)?.toInt()
                val answer = it["answer"] as String

                val callback = when (type) {
                    UserAnswer.Type.Gender -> {
                        UserAnswer.GenderAnswer(Gender.valueOf(answer))
                    }
                    UserAnswer.Type.Mmpi -> {
                        UserAnswer.Mmpi(index!!, MmpiProcess.Answer.valueOf(answer))
                    }
                    UserAnswer.Type.Lucher -> {
                        UserAnswer.Lucher(LucherColor.valueOf(answer))
                    }
                    UserAnswer.Type.NewTestRequest -> {
                        UserAnswer.NewTest(TypeOfTest.valueOf(answer))
                    }
                    UserAnswer.Type.DailyQuiz -> {
                        UserAnswer.DailyQuiz(DailyQuizOptions.valueOf(answer))
                    }
                    UserAnswer.Type.Skip -> {
                        UserAnswer.Skip()
                    }
                    UserAnswer.Type.Text -> {
                        UserAnswer.Text(answer)
                    }
                }

                sessionState.addAnswer(callback)
            }
        sessionState
    } ?: emptyList()
}

@Suppress("UNCHECKED_CAST")
private fun parseMmpiAnswers(snapshot: DataSnapshot?): List<MmpiAnswersContainer> {
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
private fun parseLucherAnswers(snapshot: DataSnapshot?): List<LucherAnswersContainer> {
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
private fun parseDailyQuizAnswers(snapshot: DataSnapshot?): List<DailyQuizAnswersContainer> {
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

private fun parseUsers(snapshot: DataSnapshot?): Map<Long, User> {
    return snapshot!!.children.associate {
        val user = it.getValue(User::class.java)
        Pair(user.id, user)
    }
}