package lucher.telegram

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import lucher.LucherColor
import mmpi.telegram.MMPI_SESSION_TEST_USER_ID
import models.Answers
import models.User

import storage.CentralDataStorage
import telegram.Button
import telegram.LaunchMode
import telegram.UserConnection

import Result
import com.soywiz.klock.DateTime
import lucher.LucherAnswers
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

const val LUCHER_SESSION_TEST_USER_ID = 2L

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LucherSessionTest {
    private lateinit var testUser: User

    @BeforeAll
    fun init() {
        CentralDataStorage.init(
            rootPath = LaunchMode.TESTS.rootPath,
            testingMode = true
        )

        CentralDataStorage.createUser(MMPI_SESSION_TEST_USER_ID, "MmpiSessionTest User")
        testUser = CentralDataStorage.users.get(MMPI_SESSION_TEST_USER_ID)!!
    }

    @AfterEach
    fun cleanUp() {
        CentralDataStorage.users.clearUser(testUser)
    }

    @Test
//    @Timeout(value = 40, unit = TimeUnit.SECONDS)
    fun start() = runBlocking {

        val resultChannel = Channel<Unit>(2)
        val lucherSession = LucherSession(
            id = LUCHER_SESSION_TEST_USER_ID,
            userConnection = object : UserConnection {

                override fun sendMessageWithButtons(
                    chatId: Long,
                    text: String,
                    buttons: List<Button>,
                    placeButtonsVertically: Boolean
                ): Long {
                    return 0
                }
            },
            onEndedCallback = {
                resultChannel.offer(Unit)
            }
        )

        lucherSession.start(user = testUser, chatId = 0L)

        val testAnswers = LucherAnswers(
            user = testUser,
            dateTime = DateTime.now(),
            firstRound = LucherColor.values().toList(),
            secondRound = LucherColor.values().toList()
        )

        LucherColor.values().dropLast(1).forEach {
            lucherSession.onCallbackFromUser(
                messageId = 0,
                data = it.name,
            )
        }

        LucherColor.values().dropLast(1).forEach {
            lucherSession.onCallbackFromUser(
                messageId = 0,
                data = it.name,
            )
        }
        resultChannel.receive()

        checkIfAnswersSavedToDatabase(testAnswers)
    }

    private fun checkIfAnswersSavedToDatabase(expectedAnswers: LucherAnswers) = runBlocking {

        val answersResult = CentralDataStorage.users.getUserAnswers(testUser)
        assertTrue(answersResult is Result.Success)

        val allAnswersFromDatabase: List<Answers> = (answersResult as Result.Success).data
        assertFalse(allAnswersFromDatabase.isEmpty())

        val databaseAnswers = allAnswersFromDatabase.first() as LucherAnswers

        assertEquals(expectedAnswers.user, databaseAnswers.user)

        val timeSpan = expectedAnswers.date - databaseAnswers.date
        assertTrue(timeSpan.seconds < 2)

        assertArrayEquals(
            expectedAnswers.firstRound.toTypedArray(),
            databaseAnswers.firstRound.toTypedArray()
        )

        assertArrayEquals(
            expectedAnswers.secondRound.toTypedArray(),
            databaseAnswers.secondRound.toTypedArray()
        )
    }
}