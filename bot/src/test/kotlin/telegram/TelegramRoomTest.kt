package telegram

import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import storage.CentralDataStorage

@InternalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TelegramRoomTest {

    @BeforeEach
    fun setup() {
        CentralDataStorage.init(
            launchMode = LaunchMode.TESTS,
            testingMode = true
        )
    }

    @Test
    fun `sessions saving`() = runBlocking {
        val originalRoom = TelegramRoom(
            roomId = 0,
            userConnection = MockUserConnection
        )
        for (id in 0..10L) {
            val chatInfo = mockChatInfo(id)
            originalRoom.welcomeUser(
                chatInfo = chatInfo,
                userConnection = MockUserConnection
            ).join()

            originalRoom.launchMmpi377Test(chatInfo).join()
        }

        assertEquals(11, originalRoom.sessions.size)
//        originalRoom.saveState()

        val restoredRoom = TelegramRoom(
            roomId = 0,
            userConnection = MockUserConnection
        )

        restoredRoom.restoreState()
        assertEquals(11, restoredRoom.sessions.size)
    }
}

object MockUserConnection : UserConnection {

}

private fun mockChatInfo(id: Long): ChatInfo {
    return ChatInfo(
        userId = id,
        userName = "user $id",
        chatId = id,
        messageId = id
    )
}