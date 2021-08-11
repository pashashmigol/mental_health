package telegram

import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import storage.CentralDataStorage
import java.util.concurrent.TimeUnit

@InternalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TelegramRoomTest {

    @BeforeAll
    fun setup() {
        CentralDataStorage.init(
            launchMode = LaunchMode.TESTS,
            testingMode = true
        )
        CentralDataStorage.usersStorage.clear()
    }

    @AfterAll
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    fun clear() {
        CentralDataStorage.usersStorage.clear()
    }

    @Test
    fun `sessions saving`() = runBlocking {
        val originalRoom = TelegramRoom(
            roomId = 0,
            userConnection = MockUserConnection
        )
        for (id in 0..36L step 4) {
            originalRoom.launchMmpi377(
                launchSession(id, originalRoom)
            ).join()
            originalRoom.launchMmpi566(
                launchSession(id + 1, originalRoom)
            ).join()
            originalRoom.launchLucher(
                launchSession(id + 2, originalRoom)
            ).join()
            originalRoom.launchDailyQuiz(
                launchSession(id + 3, originalRoom)
            ).join()
        }

        assertEquals(40, originalRoom.sessions.size)

        val restoredRoom = TelegramRoom(
            roomId = 0,
            userConnection = MockUserConnection
        )

        restoredRoom.restoreState()
        assertEquals(40, restoredRoom.sessions.size)
    }
}

object MockUserConnection : UserConnection

@InternalAPI
private suspend fun launchSession(id: Long, originalRoom: TelegramRoom): ChatInfo {
    val chatInfo = ChatInfo(
        userId = id,
        userName = "user $id",
        chatId = id,
        messageId = id
    )
    originalRoom.welcomeUser(
        chatInfo = chatInfo,
        userConnection = MockUserConnection
    ).join()

    return chatInfo
}