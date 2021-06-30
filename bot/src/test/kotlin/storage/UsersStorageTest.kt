@file:Suppress("SameParameterValue")

package storage

import Gender
import com.soywiz.klock.DateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import models.User
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Timeout
import telegram.Callback
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

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    fun `mmpi answers saving`() = runBlocking {
        val user = createUser(111L, "Pasha")

        val mockAnswers = createMmpiAnswers(user, Gender.Female)
        val saveResult = CentralDataStorage.usersStorage.saveAnswers(mockAnswers)
        assertTrue(saveResult is Result.Success)

        val receivedAnswers = (CentralDataStorage.usersStorage
            .getUserAnswers(user = user) as Result.Success)
            .data.first()

        assertEquals(mockAnswers, receivedAnswers)

        CentralDataStorage.usersStorage.clearUser(user)
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    fun `Lucher answers saving`() = runBlocking {
        val user = createUser(222L, "Pasha")

        val mockAnswers = createLucherAnswers(user)
        val saveResult = CentralDataStorage.usersStorage.saveAnswers(mockAnswers)
        assertTrue(saveResult is Result.Success)

        val receivedAnswers = (CentralDataStorage.usersStorage
            .getUserAnswers(user = user) as Result.Success)
            .data[0]

        assertEquals(mockAnswers, receivedAnswers)

        CentralDataStorage.usersStorage.clearUser(user)
    }

    @Test
    @Timeout(value = 100, unit = TimeUnit.SECONDS)
    fun `session's states saving`() = runBlocking {
        val originalSessions: List<SessionState> = createSessions()
        CentralDataStorage.usersStorage.saveAllSessions(originalSessions)

        val storageResult = CentralDataStorage.usersStorage.takeAllSessions()
        val sessionsFromStorage = (storageResult as Result.Success).data

        assertArrayEquals(originalSessions.toTypedArray(), sessionsFromStorage.toTypedArray())
    }

    private suspend fun createUser(userId: Long, userName: String): User {
        CentralDataStorage.createUser(userId, userName)

        checkUser(userId, userName)
        CentralDataStorage.reload()

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

    private fun createLucherAnswers(user: User): LucherAnswers {
        return LucherAnswers(
            user = user,
            date = DateTime.EPOCH.plus(DateTimeSpan(seconds = 2)).utc,
            firstRound = roundAnswers(),
            secondRound = roundAnswers()
        )
    }

    private fun createSessions(): List<SessionState> {
        val mmpi566 = SessionState(userId = 0, chatId = 0, roomId = 0, sessionId = 0, type = TypeOfTest.Mmpi566)
        val mmpi377 = SessionState(userId = 0, chatId = 0, roomId = 0, sessionId = 1, type = TypeOfTest.Mmpi377)
        val lucher = SessionState(userId = 0, chatId = 0, roomId = 0, sessionId = 2, type = TypeOfTest.Lucher)

        for (index in 0..20) {

            mmpi566.addAnswer(
                Callback.MmpiAnswer(
                    index = index,
                    answer = MmpiProcess.Answer.Agree
                )
            )
            mmpi377.addAnswer(
                Callback.MmpiAnswer(
                    index = index,
                    answer = MmpiProcess.Answer.Agree
                )
            )
            lucher.addAnswer(
                Callback.LucherAnswer(
                    answer = LucherColor.Gray
                )
            )
        }
        return listOf(mmpi566, mmpi377, lucher)
    }
}