package storage

import Result
import com.google.firebase.database.*
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.parse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import lucher.LucherAnswers
import lucher.LucherColor
import mmpi.MmpiAnswers
import mmpi.MmpiProcess
import models.Answers
import models.User
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap


private const val USERS = "users_info"
private const val MMPI_ANSWERS = "mmpi_answers"
private const val LUCHER_ANSWERS = "lucher_answers"

class UsersStorage(database: FirebaseDatabase) {

    private val usersInfoRef: DatabaseReference = database.reference.child(USERS)
    private val usersMmpiAnswersRef: DatabaseReference = database.reference.child(MMPI_ANSWERS)
    private val usersLucherAnswersRef: DatabaseReference = database.reference.child(LUCHER_ANSWERS)

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

    fun get(id: Long) = users[id]

    fun allUsers(): List<User> = users.values.toList()

    fun hasUserWithId(userId: Long) = users.containsKey(userId)

    fun add(user: User) {
        users[user.id] = user

        usersInfoRef
            .child(user.id.toString())
            .setValue(user) { error: DatabaseError?, ref: DatabaseReference ->
                println("add($user) ref: $ref, error: $error")
            }
    }

    fun saveAnswers(answers: MmpiAnswers) {

        val hashMap = HashMap<String, Any>().apply {
            put("date", answers.dateString)
            put("gender", answers.gender.name)

            put("user", HashMap<String, Any>().apply {
                put("id", answers.user.id)
                put("name", answers.user.name)
                put("googleDriveFolder", answers.user.googleDriveFolder)
            })
            put("answersList", answers.answersList.map { it.name })
        }

        usersMmpiAnswersRef
            .child(answers.user.id.toString())
            .child(answers.dateString)
            .setValue(hashMap) { error, ref ->
                println("add(${answers.user}) ref: $ref, error: $error")
            }
    }

    fun saveAnswers(answers: LucherAnswers) {

        val hashMap = HashMap<String, Any>().apply {
            put("date", answers.dateString)

            put("user", HashMap<String, Any>().apply {
                put("id", answers.user.id)
                put("name", answers.user.name)
                put("googleDriveFolder", answers.user.googleDriveFolder)
            })
            put("firstRound", answers.firstRound.map { it.name })
            put("secondRound", answers.secondRound.map { it.name })
        }

        usersLucherAnswersRef
            .child(answers.user.id.toString())
            .child(answers.dateString)
            .setValue(hashMap) { error, ref ->
                println("add(${answers.user}) ref: $ref, error: $error")
            }
    }

    fun clearUser(user: User) {
        usersMmpiAnswersRef
            .child(user.id.toString())
            .setValue(null) { error, ref ->
                println("add($user) ref: $ref, error: $error")
            }
        usersLucherAnswersRef
            .child(user.id.toString())
            .setValue(null) { error, ref ->
                println("add($user) ref: $ref, error: $error")
            }
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
                googleDriveFolder = it["googleDriveFolder"] as String
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
                googleDriveFolder = it["googleDriveFolder"] as String
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