package storage.users

import com.google.firebase.database.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import telegram.MessageId
import telegram.SessionId
import telegram.SessionState
import telegram.UserAnswer
import Result

private const val ACTIVE_SESSIONS = "active_sessions"


class SessionStorageFirebase(database: FirebaseDatabase) : SessionStorage {
    private val activeSessionsRef: DatabaseReference = database.reference.child(ACTIVE_SESSIONS)

    override suspend fun addSession(
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

    override suspend fun addAnswer(
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

    override suspend fun addMessageId(
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

    override suspend fun takeAllSessions(): Result<List<SessionState>> {
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

    override fun removeSession(sessionId: SessionId): Result<Unit> = runBlocking {
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

    override fun clear() {
        val resultChannel = Channel<Result<Unit>>(1)
        activeSessionsRef
            .removeValue { error, _ ->
                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
    }
}