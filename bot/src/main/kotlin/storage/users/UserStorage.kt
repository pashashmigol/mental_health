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
    ): Result<Unit>

    suspend fun addSession(
        session: SessionState
    ): Result<Unit>

    suspend fun addAnswer(
        sessionId: SessionId,
        userAnswer: UserAnswer,
        index: Int
    ): Result<Unit>

    suspend fun addMessageId(
        sessionId: SessionId,
        messageId: MessageId,
        index: Int
    ): Result<Unit>

    suspend fun takeAllSessions(): Result<List<SessionState>>
    fun clear()
    fun removeSession(sessionId: SessionId): Result<Unit>
    fun getUser(userId: Long): User?
    fun allUsers(): List<User>
    fun hasUserWithId(userId: Long): Boolean

    suspend fun saveUser(user: User): Result<Unit>

    suspend fun saveMmpiAnswers(answers: MmpiAnswersContainer): Result<Unit>

    suspend fun saveLucherAnswers(answers: LucherAnswersContainer): Result<Unit>

    suspend fun clearUser(user: User): Result<Unit>

    suspend fun getUserAnswers(user: User): Result<List<AnswersContainer>>
}