package mmpi.telegram

import kotlinx.coroutines.runBlocking
import models.TypeOfTest

import storage.CentralDataStorage
import telegram.LaunchMode

import Result
import mmpi.MmpiAnswers
import mmpi.MmpiProcess
import models.Answers
import models.User
import models.size
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import telegram.Button
import telegram.TelegramSession
import telegram.UserConnection
import java.util.concurrent.TimeUnit

const val MMPI_SESSION_TEST_USER_ID = 1L

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MmpiSessionTest {
    private lateinit var testUser: User

    @BeforeAll
    fun init() = runBlocking{
        CentralDataStorage.init(
            launchMode = LaunchMode.TESTS,
            testingMode = true
        )

        CentralDataStorage.createUser(MMPI_SESSION_TEST_USER_ID, "MmpiSessionTest User")
        testUser = CentralDataStorage.usersStorage.getUser(MMPI_SESSION_TEST_USER_ID)!!
    }

    @AfterEach
    fun cleanUp() {
        CentralDataStorage.usersStorage.clearUser(testUser)
    }

    @Test
    fun `basic case`() = runBlocking {

        var session: MmpiSession? = null
        val questionsIds = generateSequence(0L) { it + 1 }.iterator()

        session = MmpiSession(
            user = testUser,
            chatId = 1L,
            roomId = 0L,
            type = TypeOfTest.Mmpi566,
            userConnection = object : UserConnection {

                override fun sendMessageWithButtons(
                    chatId: Long,
                    text: String,
                    buttons: List<Button>,
                    placeButtonsVertically: Boolean
                ): Long {
                    return questionsIds.next()
                }
            }
        ) {
            checkSessionResult(testUser, session, it)
        }

        session.start()

        val answersIds = generateSequence(0L) { it + 1 }.iterator()

        val res = session.sendAnswer(answersIds.next(), Gender.Male.name)
        assertTrue(res is Result.Success, "$res")

        session.testingCallback = { answers ->
            assertEquals(TypeOfTest.Mmpi566.size, answers.size)
            assertTrue(answers.all { it == MmpiProcess.Answer.Agree })
            checkState(answers, session)
        }

        repeat(TypeOfTest.Mmpi566.size) {
            val id = answersIds.next()
            val answerResult = session.sendAnswer(
                messageId = id,
                data = MmpiProcess.Answer.Agree.name
            )
            assertTrue(answerResult is Result.Success, "$answerResult")
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `change answers`() = runBlocking {
        var session: MmpiSession? = null
        val questionsIds = generateSequence(0L) { it + 1 }.iterator()

        session = MmpiSession(
            user = testUser,
            chatId = 2L,
            roomId = 0L,
            type = TypeOfTest.Mmpi566,
            userConnection = object : UserConnection {
                override fun sendMessageWithButtons(
                    chatId: Long,
                    text: String,
                    buttons: List<Button>,
                    placeButtonsVertically: Boolean
                ): Long {
                    return questionsIds.next()
                }
            },
        ) {
            assertEquals(session, it)
        }
        session.testingCallback = { answers ->
            checkEditedAnswers(answers)
            checkState(answers, session)
        }

        session.start()
        val answersIds = generateSequence(0L) { it + 1 }.iterator()

        do {
            val res = session.sendAnswer(answersIds.next(), Gender.Male.name)
            assertTrue(res is Result.Success, "$res")
        } while (res is Result.Error)

        repeat(TypeOfTest.Mmpi566.size) {
            sendAnswersToSession(answersIds, session, it)
        }
    }

    private suspend fun sendAnswersToSession(
        answersIds: Iterator<Long>,
        session: MmpiSession,
        it: Int
    ) {
        val id = answersIds.next()
        val res = session.sendAnswer(
            messageId = id,
            data = MmpiProcess.Answer.Agree.name
        )
        assertTrue(res is Result.Success, "$res")

        if (it % 2 == 0) {//edit given answer
            session.sendAnswer(
                messageId = id,
                data = MmpiProcess.Answer.Disagree.name
            )
        }
    }

    private fun checkEditedAnswers(answers: List<MmpiProcess.Answer>) = runBlocking {
        assertEquals(TypeOfTest.Mmpi566.size, answers.size)

        answers.forEachIndexed { i, answer ->
            if (i % 2 == 0) {
                assertEquals(MmpiProcess.Answer.Disagree, answer, "i = $i")
            } else {
                assertEquals(MmpiProcess.Answer.Agree, answer, "i = $i")
            }
        }

        val answersResult = CentralDataStorage.usersStorage.getUserAnswers(testUser)
        assertTrue(answersResult is Result.Success)

        val answersFromDatabase: List<Answers> = (answersResult as Result.Success).data
        assertFalse(answersFromDatabase.isEmpty())

        val lastAvailableAnswers = (answersFromDatabase.first() as MmpiAnswers).answersList

        assertArrayEquals(answers.toTypedArray(), lastAvailableAnswers.toTypedArray())

        answers.zip(lastAvailableAnswers).forEach {
            assertEquals(it.first, it.second)
        }
    }
}

private fun checkState(answers: List<MmpiProcess.Answer>, session: MmpiSession?) = runBlocking {
    val sessionState = session!!.state

    assertEquals(session.roomId, sessionState.roomId)
    assertEquals(session.sessionId, sessionState.sessionId)
    assertEquals(session.type, TypeOfTest.Mmpi566)

    val restoredSession = MmpiSession(
        user = User(id = 0, name = "", googleDriveFolder = ""),
        chatId = 1L,
        roomId = sessionState.sessionId,
        type = sessionState.type,
        userConnection = object : UserConnection {},
        onEndedCallback = {}
    )

    restoredSession.testingCallback = {
        assertEquals(answers, it)
    }

    restoredSession.applyState(sessionState)
}

private fun checkSessionResult(
    user: User, session: MmpiSession?,
    telegramSession: TelegramSession<Any>
) = runBlocking {
    assertEquals(session, telegramSession)

    val answersResult = CentralDataStorage.usersStorage.getUserAnswers(user)
    assertTrue(answersResult is Result.Success)

    val answers = (answersResult as Result.Success).data
    assertFalse(answers.isEmpty())
}