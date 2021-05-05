package quiz

import kotlinx.coroutines.runBlocking
import models.User
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import storage.CentralDataStorage
import telegram.Button
import telegram.LaunchMode
import telegram.UserConnection

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DailyQuizSessionTest {

    @BeforeAll
    fun init() {
        CentralDataStorage.init(LaunchMode.TESTS)
    }

    @Test
    fun start() = runBlocking {
        val testUser = User(
            id = 0,
            name = "test_user",
            googleDriveFolderUrl = "",
            googleDriveFolderId = "",
        )
        val session = DailyQuizSession(
            user = testUser,
            userConnection = object : UserConnection {
                override fun sendMessageWithButtons(
                    chatId: Long,
                    text: String,
                    buttons: List<Button>,
                    placeButtonsVertically: Boolean
                ): Long {
                    assertFalse(text.isBlank())
                    assertEquals(5, buttons.size)

                    return 0
                }
            },
            onEndedCallback = {}, roomId = 0, chatId = 0
        )
        session.start()
    }
}