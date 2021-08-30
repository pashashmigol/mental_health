package storage.users

import lucher.LucherAnswersContainer
import mmpi.MmpiAnswersContainer
import models.AnswersContainer
import models.User
import quiz.DailyQuizAnswersContainer
import telegram.MessageId
import telegram.SessionId
import telegram.SessionState
import telegram.UserAnswer
import Result

interface UserStorage {
    suspend fun saveDailyQuizAnswers(
        user: User,
        answers: DailyQuizAnswersContainer
    ): Result<Unit> {
        throw NotImplementedError()
    }

    suspend fun addSession(
        session: SessionState
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

    suspend fun addMessageId(
        sessionId: SessionId,
        messageId: MessageId,
        index: Int
    ): Result<Unit> {
        throw NotImplementedError()
    }

    suspend fun takeAllSessions(): Result<List<SessionState>> {
        throw NotImplementedError()
    }

    fun clear() {
        throw NotImplementedError()
    }

    fun removeSession(sessionId: SessionId): Result<Unit> {
        throw NotImplementedError()
    }

    fun getUser(userId: Long): User? {
        throw NotImplementedError()
    }

    fun allUsers(): List<User> {
        throw NotImplementedError()
    }

    fun hasUserWithId(userId: Long): Boolean {
        throw NotImplementedError()
    }

    suspend fun saveUser(user: User): Result<Unit> {
        throw NotImplementedError()
    }

    suspend fun saveMmpiAnswers(answers: MmpiAnswersContainer): Result<Unit> {
        throw NotImplementedError()
    }

    suspend fun saveLucherAnswers(answers: LucherAnswersContainer): Result<Unit> {
        throw NotImplementedError()
    }

    suspend fun clearUser(user: User): Result<Unit> {
        throw NotImplementedError()
    }

    suspend fun getUserAnswers(user: User): Result<List<AnswersContainer>> {
        throw NotImplementedError()
    }
}