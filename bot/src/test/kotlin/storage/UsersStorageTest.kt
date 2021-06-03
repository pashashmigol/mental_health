@file:Suppress("SameParameterValue")

package storage

import Gender
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
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UsersStorageTest {

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

        val mockAnswers = createMmpiAnswers(user, Gender.Female)
        CentralDataStorage.usersStorage.saveAnswers(mockAnswers)

        val receivedAnswers = (CentralDataStorage.usersStorage
            .getUserAnswers(user = user) as Result.Success)
            .data.first()

        assertEquals(mockAnswers, receivedAnswers)
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `Lucher answers saving`() = runBlocking {
        val user = createUser(0L, "Pasha")

        val mockAnswers = createLucherAnswers(user)
        CentralDataStorage.usersStorage.saveAnswers(mockAnswers)

        val receivedAnswers = (CentralDataStorage.usersStorage
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
        val user = CentralDataStorage.usersStorage.get(userId)

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
            dateTime = DateTime.EPOCH.plus(DateTimeSpan(seconds = 2)).utc,
            firstRound = roundAnswers(),
            secondRound = roundAnswers()
        )
    }
}