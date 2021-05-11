@file:Suppress("SameParameterValue")

package storage

import com.soywiz.klock.DateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import models.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import telegram.LaunchMode
import Result
import com.soywiz.klock.DateTimeSpan
import lucher.LucherAnswers
import lucher.roundAnswers
import mmpi.MmpiAnswers
import mmpi.justFewAnswers
import models.Type
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UsersTest {

    @BeforeAll
    fun init() {
        CentralDataStorage.init(
            rootPath = LaunchMode.TESTS.rootPath,
            testingMode = true
        )
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `mmpi answers saving`() = runBlocking {
        val user = createUser(0L, "Pasha")

        val mockAnswers = createMmpiAnswers(user)
        CentralDataStorage.users.saveAnswers(user = user, answers = mockAnswers)

        val receivedAnswers = (CentralDataStorage.users
            .getUserAnswers(user = user) as Result.Success)
            .data.first()

        assertEquals(mockAnswers, receivedAnswers)
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `Lucher answers saving`() = runBlocking {
        val user = createUser(0L, "Pasha")

        val mockAnswers = createLucherAnswers(user)
        CentralDataStorage.users.saveAnswers(user = user, answers = mockAnswers)

        val receivedAnswers = (CentralDataStorage.users
            .getUserAnswers(user = user) as Result.Success)
            .data[1]

        assertEquals(mockAnswers, receivedAnswers)
    }

    private suspend fun createUser(userId: Long, userName: String): User {
        CentralDataStorage.createUser(userId, userName)

        checkUser(userId, userName)
        CentralDataStorage.reload()

        delay(1000)
        return checkUser(userId, userName)
    }

    private fun checkUser(userId: Long, userName: String): User {
        val user = CentralDataStorage.users.get(userId)

        assert(user != null)
        assertEquals(userId, user?.id)
        assertEquals(userName, user?.name)

        return user!!
    }

    private fun createMmpiAnswers(user: User): MmpiAnswers {
        return MmpiAnswers(
            user = user,
            dateTime = DateTime.EPOCH.plus(DateTimeSpan(seconds = 1)),
            answers = justFewAnswers
        )
    }

    private fun createLucherAnswers(user: User): LucherAnswers {
        return LucherAnswers(
            user = user,
            dateTime = DateTime.EPOCH.plus(DateTimeSpan(seconds = 2)),
            firstRound = roundAnswers(),
            secondRound = roundAnswers()
        )
    }
}