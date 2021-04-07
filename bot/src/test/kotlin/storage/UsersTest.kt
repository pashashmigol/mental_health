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
        CentralDataStorage.users.clear()
    }

    @Test
    fun `simple usage`() = runBlocking {
        val users = CentralDataStorage.users.allUsers()

        assert(users.isEmpty())

        val userName = "Pasha"
        val userId = 0L

        CentralDataStorage.createUser(userId, userName)

        val user = CentralDataStorage.users.get(userId)

        assert(user != null)
        assertEquals(user?.id, userId)
        assertEquals(user?.name, userName)

        assertEquals(1, CentralDataStorage.users.allUsers().size)

        CentralDataStorage.users.clear()

        assert(CentralDataStorage.users.allUsers().isEmpty())
        delay(1000)
        assert(CentralDataStorage.users.allUsers().isEmpty())
    }
}