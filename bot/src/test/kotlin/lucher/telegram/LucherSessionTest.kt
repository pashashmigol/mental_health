package lucher.telegram

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import lucher.LucherColor
import models.Answers
import models.User

import storage.CentralDataStorage
import telegram.Button
import telegram.LaunchMode
import telegram.UserConnection

import Result
import com.soywiz.klock.DateTimeTz
import lucher.LucherAnswers
import models.TypeOfTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.concurrent.TimeUnit

const val LUCHER_SESSION_TEST_USER_ID = 2L

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LucherSessionTest {
    private lateinit var testUser: User

    @BeforeAll
    fun init() {
        CentralDataStorage.init(
            launchMode = LaunchMode.TESTS,
            testingMode = true
        )

        CentralDataStorage.createUser(LUCHER_SESSION_TEST_USER_ID, "LucherSessionTest User")
        testUser = CentralDataStorage.usersStorage.getUser(LUCHER_SESSION_TEST_USER_ID)!!
    }

    @AfterAll
    fun cleanUp() {
        CentralDataStorage.usersStorage.clearUser(testUser)
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `basic case`() = runBlocking {

        val resultChannel = Channel<Unit>(2)
        val lucherSession = createMockSession(resultChannel)

        lucherSession.start()

        val testAnswers = LucherAnswers(
            user = testUser,
            date = DateTimeTz.nowLocal(),
            firstRound = LucherColor.values().toList(),
            secondRound = LucherColor.values().toList()
        )

        //complete first round
        LucherColor.values().forEach {
            lucherSession.sendAnswer(
                messageId = 0,
                data = it.name,
            )
        }

        //complete second round
        LucherColor.values().forEach {
            lucherSession.sendAnswer(
                messageId = 0,
                data = it.name,
            )
        }
        resultChannel.receive()

        checkAnswersSavedToDatabase(testUser, testAnswers)
        checkState(lucherSession)
    }
}

private fun checkState(session: LucherSession) {
    val sessionState = session.state

    assertEquals(session.roomId, sessionState.roomId)
    assertEquals(session.sessionId, sessionState.sessionId)

    assertEquals(session.type, TypeOfTest.Lucher)
    assertEquals(16, sessionState.messages.size)
}

private fun createMockSession(resultChannel: Channel<Unit>) = LucherSession(
    roomId = 0L,
    userConnection = object : UserConnection {
        override fun sendMessageWithButtons(
            chatId: Long,
            text: String,
            buttons: List<Button>,
            placeButtonsVertically: Boolean
        ): Long {
            return 0
        }

        override fun notifyAdmin(text: String, exception: Throwable?) {
            println(text)
            exception?.let { fail(it) }
        }
    },
    onEndedCallback = {
        resultChannel.offer(Unit)
    },
    minutesBetweenRounds = 2,
    user = CentralDataStorage.usersStorage.getUser(LUCHER_SESSION_TEST_USER_ID)!!,
    chatId = 0
)

private fun checkAnswersSavedToDatabase(
    user: User,
    expectedAnswers: LucherAnswers
) = runBlocking {

    val answersResult = CentralDataStorage.usersStorage.getUserAnswers(user)
    assertTrue(answersResult is Result.Success)

    val allAnswersFromDatabase: List<Answers> = (answersResult as Result.Success).data
    assertFalse(allAnswersFromDatabase.isEmpty())

    val databaseAnswers = allAnswersFromDatabase.first() as LucherAnswers

    assertEquals(expectedAnswers.user, databaseAnswers.user)

    val timeSpan = expectedAnswers.date - databaseAnswers.date
    assertTrue(timeSpan.seconds < 5)

    assertArrayEquals(
        expectedAnswers.firstRound.toTypedArray(),
        databaseAnswers.firstRound.toTypedArray()
    )

    assertArrayEquals(
        expectedAnswers.secondRound.toTypedArray(),
        databaseAnswers.secondRound.toTypedArray()
    )
}