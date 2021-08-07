package quiz

import kotlinx.coroutines.runBlocking
import models.User
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import storage.CentralDataStorage
import telegram.Button
import telegram.QuizButton
import telegram.LaunchMode
import telegram.UserConnection
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DailyQuizSessionTest {

    @BeforeAll
    fun init() {
        CentralDataStorage.init(LaunchMode.TESTS)
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun start() = runBlocking {
//        val testUser = User(
//            id = 0,
//            name = "test_user",
//            googleDriveFolderUrl = "",
//            googleDriveFolderId = "",
//        )
//        val session = DailyQuizSession(
//            user = testUser,
//            userConnection = object : UserConnection {
//                override fun sendMessageWithButtons(
//                    chatId: Long,
//                    text: String,
//                    buttons: List<Button>,
//                    placeButtonsVertically: Boolean
//                ): Long {
//                    assertFalse(text.isBlank())
//                    assertEquals(5, buttons.size)
//
//                    return 0
//                }
//            },
//            onEndedCallback = {}, roomId = 0, chatId = 0
//        )
//        session.start()
//
//        repeat(4) {
//            session.sendAnswer(
//                quizButton = QuizButton.DailyQuiz(
//                    answer = DailyQuizAnswer.AWFUL
//                ),
//                messageId = it.toLong()
//            )
//        }
    }
}