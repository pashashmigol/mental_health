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
import lucher.LucherAnswers
import lucher.LucherColor
import mmpi.MmpiAnswers
import mmpi.MmpiProcess
import models.Answers
import models.TypeOfTest
import models.User
import telegram.*
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap


private const val USERS = "users_info"
private const val MMPI_ANSWERS = "mmpi_answers"
private const val LUCHER_ANSWERS = "lucher_answers"
private const val ACTIVE_SESSIONS = "active_sessions"

class UsersStorage(database: FirebaseDatabase) {

    private val usersInfoRef: DatabaseReference = database.reference.child(USERS)
    private val usersMmpiAnswersRef: DatabaseReference = database.reference.child(MMPI_ANSWERS)
    private val usersLucherAnswersRef: DatabaseReference = database.reference.child(LUCHER_ANSWERS)
    private val activeSessionsRef: DatabaseReference = database.reference.child(ACTIVE_SESSIONS)

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
        callback: Callback,
        index: Int
    ): Result<Unit> {
        val resultChannel = Channel<Result<Unit>>(1)

        activeSessionsRef
            .child(sessionId.toString())
            .child("answers")
            .child(index.toString())
            .setValue(callback) { error: DatabaseError?, _: DatabaseReference ->
                println("addAnswer(); callback $callback")

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

//    suspend fun saveAllSessions(sessions: List<SessionState>): Result<Unit> {
//        val resultChannel = Channel<Result<Unit>>(1)
//        sessions.forEach {
//            activeSessionsRef
//                .child(it.sessionId.toString())
//                .setValue(it) { error: DatabaseError?, ref: DatabaseReference ->
//                    println("saveAllSessions(); session $it, ref: $ref, error: $error")
//
//                    val result = when (error == null) {
//                        true -> Result.Success(Unit)
//                        false -> Result.Error(error.details)
//                    }
//                    resultChannel.offer(result)
//                }
//        }
//        return resultChannel.receive()
//    }

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

    suspend fun clear(): Result<Unit> {
        val resultChannel = Channel<Result<Unit>>(1)
        activeSessionsRef
            .removeValue { error, ref ->
                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
        return resultChannel.receive()
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

    suspend fun saveAnswers(answers: MmpiAnswers): Result<Unit> {
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

        usersMmpiAnswersRef
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

    suspend fun saveAnswers(answers: LucherAnswers): Result<Unit> {
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
        usersLucherAnswersRef
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
        usersMmpiAnswersRef
            .child(user.id.toString())
            .setValue(null) { error, ref ->
                println("add($user) ref: $ref, error: $error")
                resultChannel.offer(Unit)
            }
        usersLucherAnswersRef
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

    suspend fun getUserAnswers(user: User): Result<List<Answers>> {
        val resultChannel = Channel<List<Answers>>(2)

        usersMmpiAnswersRef
            .child(user.id.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot?) {
                    println("getUserAnswers():onDataChange(): count = ${snapshot?.childrenCount}")
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

        usersLucherAnswersRef
            .child(user.id.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot?) {
                    println("getUserAnswers():onDataChange(): count = ${snapshot?.childrenCount}")
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

        return try {
            val resultList = resultChannel.receive() + resultChannel.receive()
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
                val type = Callback.Type.valueOf(it["type"] as String)
                val index = (it["index"] as? Long)?.toInt()
                val answer = it["answer"] as String

                val callback = when (type) {
                    Callback.Type.Gender -> Callback.GenderAnswer(Gender.valueOf(answer))
                    Callback.Type.Mmpi -> Callback.MmpiAnswer(index!!, MmpiProcess.Answer.valueOf(answer))
                    Callback.Type.Lucher -> Callback.LucherAnswer(LucherColor.valueOf(answer))
                    Callback.Type.NewTestRequest -> Callback.NewTestRequest(TypeOfTest.valueOf(answer))
                }

                sessionState.addAnswer(callback)
            }
        sessionState
    } ?: emptyList()
}

@Suppress("UNCHECKED_CAST")
private fun parseMmpiAnswers(snapshot: DataSnapshot?): List<MmpiAnswers> {
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
                googleDriveFolderUrl = it["googleDriveFolder"] as String
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
        MmpiAnswers(
            user = user,
            date = date,
            gender = gender,
            answersList = answers
        )
    } ?: emptyList()
}

@Suppress("UNCHECKED_CAST")
private fun parseLucherAnswers(snapshot: DataSnapshot?): List<LucherAnswers> {
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
                googleDriveFolderUrl = it["googleDriveFolder"] as String
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
        LucherAnswers(
            user = user,
            date = date,
            firstRound = firstRound,
            secondRound = secondRound
        )
    } ?: return emptyList()
}

private fun parseUsers(snapshot: DataSnapshot?): Map<Long, User> {
    return snapshot!!.children.associate {
        val user = it.getValue(User::class.java)
        Pair(user.id, user)
    }
}