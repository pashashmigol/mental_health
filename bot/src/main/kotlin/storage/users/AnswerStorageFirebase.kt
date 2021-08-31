package storage.users

import com.google.firebase.database.*
import com.soywiz.klock.DateFormat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import lucher.LucherAnswersContainer
import mmpi.MmpiAnswersContainer
import models.AnswersContainer
import models.User
import quiz.DailyQuizAnswersContainer
import java.util.concurrent.CancellationException

import Result

private const val MMPI_ANSWERS = "mmpi_answers"
private const val LUCHER_ANSWERS = "lucher_answers"
private const val DAILY_QUIZ_ANSWERS = "daily_quiz_answers"


class AnswerStorageFirebase(database: FirebaseDatabase) : AnswerStorage {
    private val mmpiAnswersRef: DatabaseReference = database.reference.child(MMPI_ANSWERS)
    private val lucherAnswersRef: DatabaseReference = database.reference.child(LUCHER_ANSWERS)
    private val dailyQuizAnswersRef: DatabaseReference = database.reference.child(DAILY_QUIZ_ANSWERS)

    override suspend fun getUserAnswers(user: User): Result<List<AnswersContainer>> {
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

    override suspend fun saveAnswers(answers: AnswersContainer): Result<Unit> {
        return when (answers) {
            is DailyQuizAnswersContainer -> saveDailyQuizAnswers(answers)
            is MmpiAnswersContainer -> saveMmpiAnswers(answers)
            is LucherAnswersContainer -> saveLucherAnswers(answers)
            else -> Result.Error(
                "UsersStorageFirebase.saveAnswers() error: not implemented for ${answers::javaClass.name}"
            )
        }
    }

    private suspend fun saveDailyQuizAnswers(
        answers: DailyQuizAnswersContainer
    ): Result<Unit> {
        val dateString = answers.date.toString(DateFormat.DEFAULT_FORMAT)
        val user = answers.user

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

    private suspend fun saveMmpiAnswers(answers: MmpiAnswersContainer): Result<Unit> {
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

    private suspend fun saveLucherAnswers(answers: LucherAnswersContainer): Result<Unit> {
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

    override fun clear() {
        val resultChannel = Channel<Result<Unit>>(5)

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
    }
}


