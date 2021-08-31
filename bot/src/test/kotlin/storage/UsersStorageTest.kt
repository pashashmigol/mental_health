@file:Suppress("SameParameterValue")

package storage

import Gender
import com.soywiz.klock.DateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import models.User

import Result
import com.soywiz.klock.DateTimeSpan
import lucher.LucherAnswersContainer
import lucher.LucherColor
import lucher.roundAnswers
import mmpi.MmpiAnswersContainer
import mmpi.MmpiProcess
import mmpi.justFewAnswers
import models.AnswersContainer
import models.TypeOfTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assertions.fail
import org.kodein.di.instance
import quiz.DailyQuizAnswer
import quiz.DailyQuizAnswersContainer
import quiz.DailyQuizOptions
import storage.users.*
import telegram.UserAnswer
import telegram.SessionState
import testDI
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UsersStorageTest {
    private val userStorage: UserStorage by testDI.instance()
    private val answerStorage: AnswerStorage by testDI.instance()
    private val reportStorage: GoogleDriveReportStorage by testDI.instance()
    private val sessionStorage: SessionStorage by testDI.instance()

    @BeforeAll
    fun init() {
        userStorage.clear()
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    fun `mmpi answers saving`() = runBlocking {
        val user = createUser(333L, "mmpi answers saving")

        val mockAnswers = createMmpiAnswers(user, Gender.Female)
        val saveResult = answerStorage.saveAnswers(mockAnswers)
        assertTrue(saveResult is Result.Success)

        val receivedAnswers = answerStorage
            .getUserAnswers(user = user)
            .dealWithError {
                fail(it.exception)
            }
            .first()

        assertEquals(mockAnswers, receivedAnswers)
    }

    @Test
    @Timeout(value = 40, unit = TimeUnit.SECONDS)
    fun `Lucher answers saving`() = runBlocking {
        val user = createUser(222L, "Lucher answers saving")

        val mockAnswers = createLucherAnswers(user)
        val saveResult = answerStorage.saveAnswers(mockAnswers)
        assertTrue(saveResult is Result.Success)

        val receivedAnswers = answerStorage
            .getUserAnswers(user)
            .dealWithError {
                fail(it.exception)
            }
            .first()

        assertEquals(mockAnswers, receivedAnswers)
    }

    @Test
    @Timeout(value = 40, unit = TimeUnit.SECONDS)
    fun `session's states saving`() = runBlocking {
        val originalSessions: List<SessionState> = createSessions()

        originalSessions.forEach {
            it.addToStorage(sessionStorage)
        }

        val storageResult = sessionStorage.takeAllSessions()

        if (storageResult is Result.Error) {
            fail<Exception>(storageResult.exception)
        }
        val sessionsFromStorage = (storageResult as Result.Success).data
        assertArrayEquals(originalSessions.toTypedArray(), sessionsFromStorage.toTypedArray())
    }

    @Test
    @Timeout(value = 40, unit = TimeUnit.SECONDS)
    fun `daily quiz answers saving`() = runBlocking {
        val user = createUser(334L, "daily quiz answers saving")

        val mockAnswers = createDailyQuizAnswers(user)
        val saveResult = answerStorage.saveAnswers(
            answers = mockAnswers
        )

        assertTrue(saveResult is Result.Success)

        val receivedAnswers: AnswersContainer = answerStorage
            .getUserAnswers(user = user)
            .dealWithError {
                fail(it.exception)
            }
            .first()

        assertEquals(mockAnswers, receivedAnswers)
    }

    private suspend fun createUser(userId: Long, userName: String): User {
        createUser(userId, userName, reportStorage, userStorage)
        checkUser(userId, userName)

        delay(1000)
        return checkUser(userId, userName)
    }

    private fun checkUser(userId: Long, userName: String): User {
        val user = userStorage.getUser(userId)

        assert(user != null)
        assertEquals(userId, user?.id)
        assertEquals(userName, user?.name)

        return user!!
    }

    private fun createMmpiAnswers(user: User, gender: Gender): MmpiAnswersContainer {
        return MmpiAnswersContainer(
            user = user,
            date = DateTime.EPOCH.plus(DateTimeSpan(seconds = 1)).utc,
            gender = gender,
            answersList = justFewAnswers
        )
    }

    private fun createDailyQuizAnswers(user: User): DailyQuizAnswersContainer {
        val date = DateTime.EPOCH.plus(DateTimeSpan(seconds = 5)).utc

        return DailyQuizAnswersContainer(
            user = user,
            date = date,
            answers = listOf(
                createDailyQuizAnswer(0),
                createDailyQuizAnswer(1),
                createDailyQuizAnswer(2)
            )
        )
    }

    private fun createDailyQuizAnswer(index: Int) = DailyQuizAnswer.Option(
        questionIndex = index,
        questionText = "question $index",
        option = DailyQuizOptions.AWFUL
    )

    private fun createLucherAnswers(user: User): LucherAnswersContainer {
        return LucherAnswersContainer(
            user = user,
            date = DateTime.EPOCH.plus(DateTimeSpan(seconds = 2)).utc,
            firstRound = roundAnswers(),
            secondRound = roundAnswers()
        )
    }

    private suspend fun createSessions(): List<SessionState> {
        val mmpi566 = SessionState(
            userId = 0,
            chatId = 0,
            roomId = 0,
            sessionId = 0,
            type = TypeOfTest.Mmpi566
        )
        val mmpi377 = SessionState(
            userId = 0,
            chatId = 0,
            roomId = 0,
            sessionId = 1,
            type = TypeOfTest.Mmpi377
        )
        val lucher = SessionState(
            userId = 0,
            chatId = 0,
            roomId = 0,
            sessionId = 2,
            type = TypeOfTest.Lucher
        )

        for (index in 0..20) {
            mmpi566.saveMessageId(
                messageId = index + 1L,
                sessionStorage = sessionStorage
            )
            mmpi566.saveAnswer(
                userAnswer = UserAnswer.Mmpi(
                    index = index,
                    answer = MmpiProcess.Answer.Agree
                ),
                sessionStorage = sessionStorage
            )
            mmpi377.saveMessageId(
                messageId = index * 2 + 1L,
                sessionStorage = sessionStorage
            )
            mmpi377.saveAnswer(
                userAnswer = UserAnswer.Mmpi(
                    index = index,
                    answer = MmpiProcess.Answer.Agree
                ),
                sessionStorage = sessionStorage
            )
            lucher.saveMessageId(
                messageId = index * 3 + 1L,
                sessionStorage = sessionStorage
            )
            lucher.saveAnswer(
                userAnswer = UserAnswer.Lucher(
                    answer = LucherColor.Gray
                ),
                sessionStorage = sessionStorage
            )
        }
        return listOf(mmpi566, mmpi377, lucher)
    }
}