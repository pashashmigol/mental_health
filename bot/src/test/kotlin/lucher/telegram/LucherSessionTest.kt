package lucher.telegram

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import lucher.LucherColor
import models.AnswersContainer
import models.User

import Result
import com.soywiz.klock.DateTimeTz
import lucher.LucherAnswersContainer
import lucher.LucherData
import models.TypeOfTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.kodein.di.instance
import storage.GoogleDriveConnection
import storage.GoogleDriveReportStorage
import storage.users.UserStorage
import storage.users.createUser
import storage.users.deleteUser
import telegram.*
import testDI
import java.util.concurrent.TimeUnit

const val LUCHER_SESSION_TEST_USER_ID = 444L

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LucherSessionTest {
    private lateinit var testUser: User

    private val userStorage: UserStorage by testDI.instance()
    private val reportStorage: GoogleDriveReportStorage by testDI.instance()
    private val connection: GoogleDriveConnection by testDI.instance()
    private val lucherData: LucherData by testDI.instance()

    @BeforeAll
    fun init() = runBlocking {

        val res = createUser(
            LUCHER_SESSION_TEST_USER_ID,
            "LucherSessionTest User",
            userStorage = userStorage,
            reportStorage = reportStorage
        )
        assertTrue(res is Result.Success<Unit>)

        testUser = userStorage.getUser(LUCHER_SESSION_TEST_USER_ID)!!
    }

    @AfterAll
    fun cleanUp() = runBlocking {
        deleteUser(
            testUser,
            userStorage = userStorage,
            connection = connection
        )
        Unit
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    fun `basic case`() = runBlocking {
        val resultChannel = Channel<Unit>(1)
        val lucherSession = createMockSession(resultChannel, userStorage, reportStorage, lucherData)

        lucherSession.start()

        val testAnswers = LucherAnswersContainer(
            user = testUser,
            date = DateTimeTz.nowLocal(),
            firstRound = LucherColor.values().toList(),
            secondRound = LucherColor.values().toList()
        )

        //complete first round
        LucherColor.values().forEachIndexed { i: Int, color: LucherColor ->
            lucherSession.sendAnswer(
                UserAnswer.Lucher(answer = color),
                messageId = i.toLong()
            )
        }

        //complete second round
        LucherColor.values().forEachIndexed { i: Int, color: LucherColor ->
            lucherSession.sendAnswer(
                userAnswer = UserAnswer.Lucher(color),
                messageId = i.toLong()
            )
        }
        resultChannel.receive()

        checkAnswersSavedToDatabase(
            user = testUser,
            expectedAnswers = testAnswers,
            userStorage = userStorage,
        )
        checkState(lucherSession)
    }
}

private fun checkState(session: LucherSession) {
    val sessionState = session.state

    assertEquals(session.roomId, sessionState.roomId)
    assertEquals(session.sessionId, sessionState.sessionId)

    assertEquals(session.type, TypeOfTest.Lucher)
    assertEquals(16, sessionState.answers.size)
}

private fun createMockSession(
    resultChannel: Channel<Unit>,
    userStorage: UserStorage,
    reportStorage: GoogleDriveReportStorage,
    lucherData: LucherData
) = LucherSession(
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

        override fun notifyAdmin(text: String, exception: Throwable?): MessageId {
            println(text)
            exception?.let { fail(it) }
            return NOT_SENT
        }
    },
    onEndedCallback = {
        resultChannel.offer(Unit)
    },
    minutesBetweenRounds = 0,
    user = userStorage.getUser(LUCHER_SESSION_TEST_USER_ID)!!,
    chatId = 0,
    userStorage = userStorage,
    reportStorage = reportStorage,
    lucherData = lucherData
)

private fun checkAnswersSavedToDatabase(
    user: User,
    expectedAnswers: LucherAnswersContainer,
    userStorage: UserStorage
) = runBlocking {

    val answersResult = userStorage.getUserAnswers(user)
    assertTrue(answersResult is Result.Success)

    val allAnswersContainerFromDatabase: List<AnswersContainer> = (answersResult as Result.Success).data
    assertFalse(allAnswersContainerFromDatabase.isEmpty())

    val databaseAnswers = allAnswersContainerFromDatabase.first() as LucherAnswersContainer

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