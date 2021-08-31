package storage.users

import models.AnswersContainer
import models.User
import telegram.MessageId
import telegram.SessionId
import telegram.SessionState
import telegram.UserAnswer
import Result


interface Storage {
    fun clear() {
        throw NotImplementedError()
    }
}

interface SessionStorage : Storage {
    suspend fun addSession(
        session: SessionState
    ): Result<Unit> {
        throw NotImplementedError()
    }

    suspend fun takeAllSessions(): Result<List<SessionState>> {
        throw NotImplementedError()
    }

    fun removeSession(sessionId: SessionId): Result<Unit> {
        throw NotImplementedError()
    }

    suspend fun addMessageId(
        sessionId: SessionId,
        messageId: MessageId,
        index: Int
    ): Result<Unit> {
        throw NotImplementedError()
    }

    suspend fun addAnswer(
        sessionId: SessionId,
        userAnswer: UserAnswer,
        index: Int
    ): Result<Unit> {
        throw NotImplementedError()
    }
}

interface AnswerStorage : Storage {
    suspend fun getUserAnswers(user: User): Result<List<AnswersContainer>> {
        throw NotImplementedError()
    }

    suspend fun saveAnswers(answers: AnswersContainer): Result<Unit> {
        throw NotImplementedError()
    }
}

interface UserStorage : Storage {
    fun getUser(userId: Long): User? {
        throw NotImplementedError()
    }

    fun allUsers(): List<User> {
        throw NotImplementedError()
    }

    fun hasUser(userId: Long): Boolean {
        throw NotImplementedError()
    }

    suspend fun saveUser(user: User): Result<Unit> {
        throw NotImplementedError()
    }
}