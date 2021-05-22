package storage

import Result
import com.google.firebase.database.*
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

class Users(database: FirebaseDatabase) {

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

    fun saveAnswers(user: User, answers: Answers) {
        val userAnswersRef = when (answers) {
            is MmpiAnswers -> usersMmpiAnswersRef
            is LucherAnswers -> usersLucherAnswersRef
            else -> usersLucherAnswersRef
        }

        userAnswersRef
            .child(user.id.toString())
            .child(answers.dateString)
            .setValue(answers.data) { error, ref ->
                println("add($user) ref: $ref, error: $error")
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
                        resultChannel.offer(parseMmpiAnswers(user, snapshot))
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
                        resultChannel.offer(parseLucherAnswers(user, snapshot))
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
            Result.Success(resultChannel.receive() + resultChannel.receive())
        } catch (e: ClosedReceiveChannelException) {
            println("getUserAnswers(): ClosedReceiveChannelException: ${e.message}")
            Result.Error("Get user $user answers failed with error")
        } catch (e: CancellationException) {
            println("getUserAnswers(): CancellationException: ${e.message}")
            Result.Error("Get user $user answers was cancelled")
        }
    }
}

private fun parseMmpiAnswers(user: User, snapshot: DataSnapshot?): List<Answers> {
    val typeIndicator: GenericTypeIndicator<HashMap<String, Any>> =
        object : GenericTypeIndicator<HashMap<String, Any>>() {}

    return snapshot?.getValue(typeIndicator)?.map { entry ->
        val answers = (entry.value as List<*>).map {
            MmpiProcess.Answer.valueOf(it as String)
        }
        MmpiAnswers(
            user = user,
            dateString = entry.key,
            answers = answers
        )
    } ?: return emptyList()
}

private fun parseLucherAnswers(user: User, snapshot: DataSnapshot?): List<Answers> {
    val typeIndicator: GenericTypeIndicator<HashMap<String, Any>> =
        object : GenericTypeIndicator<HashMap<String, Any>>() {}

    return snapshot?.getValue(typeIndicator)?.map { entry ->
        val answers = entry.value as Map<String, List<String>>

        val firstRound = answers["firstRound"]!!.map { LucherColor.valueOf(it) }
        val secondRound = answers["secondRound"]!!.map { LucherColor.valueOf(it) }

        LucherAnswers(
            user = user,
            date = entry.key,
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