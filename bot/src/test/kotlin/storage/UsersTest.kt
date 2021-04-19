@file:Suppress("SameParameterValue")

package storage

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import telegram.LaunchMode

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UsersTest {

    @BeforeAll
    fun init() {
        CentralDataStorage.init(LaunchMode.TESTS.rootPath)
    }

    @Test
    fun `simple usage`() = runBlocking {
        val users = CentralDataStorage.users.allUsers()

        assert(users.isEmpty())

        val userName = "Pasha"
        val userId = 0L

        CentralDataStorage.createUser(userId, userName)

        checkUser(userId, userName)
        CentralDataStorage.reload()

        delay(1000)
        checkUser(userId, userName)
    }

    private fun checkUser(userId: Long, userName: String) {
        val user = CentralDataStorage.users.get(userId)
        assert(user != null)
        assertEquals(user?.id, userId)
        assertEquals(user?.name, userName)
    }
}