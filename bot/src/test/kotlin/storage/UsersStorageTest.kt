@file:Suppress("SameParameterValue")

package storage

import Gender
import com.soywiz.klock.DateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import models.User

import telegram.LaunchMode
import Result
import com.soywiz.klock.DateTimeSpan
import lucher.LucherAnswers
import lucher.LucherColor
import lucher.roundAnswers
import mmpi.MmpiAnswers
import mmpi.MmpiProcess
import mmpi.justFewAnswers
import models.TypeOfTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assertions.fail
import quiz.DailyQuizAnswers
import telegram.QuizButton
import telegram.SessionState
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UsersStorageTest {

    @BeforeAll
    fun init() {
        CentralDataStorage.init(
            launchMode = LaunchMode.TESTS,
            testingMode = true
        )
    }

    @BeforeEach
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    fun clear() = runBlocking{
        CentralDataStorage.usersStorage.clear()
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    fun `mmpi answers saving`() = runBlocking {
        val user = createUser(333L, "mmpi answers saving")

        val mockAnswers = createMmpiAnswers(user, Gender.Female)
        val saveResult = CentralDataStorage.usersStorage.saveMmpiAnswers(mockAnswers)
        assertTrue(saveResult is Result.Success)

        val receivedAnswers = (CentralDataStorage.usersStorage
            .getUserAnswers(user = user) as Result.Success)
            .data.first()

        assertEquals(mockAnswers, receivedAnswers)
        CentralDataStorage.deleteUser(user)
        Unit
    }

    @Test
    @Timeout(value = 40, unit = TimeUnit.SECONDS)
    fun `Lucher answers saving`() = runBlocking {
        val user = createUser(222L, "Lucher answers saving")

        val mockAnswers = createLucherAnswers(user)
        val saveResult = CentralDataStorage.usersStorage.saveLucherAnswers(mockAnswers)
        assertTrue(saveResult is Result.Success)

        val receivedAnswers = (CentralDataStorage.usersStorage
            .getUserAnswers(user = user) as Result.Success)
            .data[0]

        assertEquals(mockAnswers, receivedAnswers)
        CentralDataStorage.deleteUser(user)
        Unit
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    fun `session's states saving`() = runBlocking {
        val originalSessions: List<SessionState> = createSessions()
        val storageResult = CentralDataStorage.usersStorage.takeAllSessions()

        if (storageResult is Result.Error) {
            fail<Exception>(storageResult.exception)
        }
        val sessionsFromStorage = (storageResult as Result.Success).data
        assertArrayEquals(originalSessions.toTypedArray(), sessionsFromStorage.toTypedArray())
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    fun `daily quiz answers saving`() = runBlocking {
        val user = createUser(334L, "daily quiz answers saving")

        val mockAnswers = createDailyQuizAnswers(user)
        val saveResult = CentralDataStorage.usersStorage.saveDailyQuizAnswers(
            user = user,
            answers = mockAnswers
        )

        assertTrue(saveResult is Result.Success)

        val receivedAnswers = (CentralDataStorage.usersStorage
            .getUserAnswers(user = user) as Result.Success)
            .data.first()

        assertEquals(mockAnswers, receivedAnswers)
        CentralDataStorage.deleteUser(user)

        Unit
    }

    private suspend fun createUser(userId: Long, userName: String): User {
        CentralDataStorage.createUser(userId, userName)
        checkUser(userId, userName)

        delay(1000)
        return checkUser(userId, userName)
    }

    private fun checkUser(userId: Long, userName: String): User {
        val user = CentralDataStorage.usersStorage.getUser(userId)

        assert(user != null)
        assertEquals(userId, user?.id)
        assertEquals(userName, user?.name)

        return user!!
    }

    private fun createMmpiAnswers(user: User, gender: Gender): MmpiAnswers {
        return MmpiAnswers(
            user = user,
            date = DateTime.EPOCH.plus(DateTimeSpan(seconds = 1)).utc,
            gender = gender,
            answersList = justFewAnswers
        )
    }

    private fun createDailyQuizAnswers(user: User): DailyQuizAnswers {
        val date = DateTime.EPOCH.plus(DateTimeSpan(seconds = 5)).utc

        return DailyQuizAnswers(
            user = user,
            date = date,
            answers = listOf(
                createDailyQuizAnswer(0),
                createDailyQuizAnswer(1),
                createDailyQuizAnswer(2)
            )
        )
    }

    private fun createDailyQuizAnswer(index: Int) = DailyQuizAnswers.Answer(
        questionIndex = index,
        questionText = "question $index",
        option = DailyQuizAnswers.Option.AWFUL
    )

    private fun createLucherAnswers(user: User): LucherAnswers {
        return LucherAnswers(
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
        ).apply { addToStorage() }

        val mmpi377 = SessionState(
            userId = 0,
            chatId = 0,
            roomId = 0,
            sessionId = 1,
            type = TypeOfTest.Mmpi377
        ).apply { addToStorage() }

        val lucher = SessionState(
            userId = 0,
            chatId = 0,
            roomId = 0,
            sessionId = 2,
            type = TypeOfTest.Lucher
        ).apply { addToStorage() }

        for (index in 0..20) {

            mmpi566.saveMessageId(index + 1L)
            mmpi566.saveAnswer(
                QuizButton.Mmpi(
                    index = index,
                    answer = MmpiProcess.Answer.Agree
                )
            )
            mmpi377.saveMessageId(index * 2 + 1L)
            mmpi377.saveAnswer(
                QuizButton.Mmpi(
                    index = index,
                    answer = MmpiProcess.Answer.Agree
                )
            )
            lucher.saveMessageId(index * 3 + 1L)
            lucher.saveAnswer(
                QuizButton.Lucher(
                    answer = LucherColor.Gray
                )
            )
        }
        return listOf(mmpi566, mmpi377, lucher)
    }
}