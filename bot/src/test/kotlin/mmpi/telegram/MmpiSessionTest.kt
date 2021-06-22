package mmpi.telegram

import kotlinx.coroutines.runBlocking
import models.TypeOfTest

import storage.CentralDataStorage
import telegram.LaunchMode
import java.util.concurrent.TimeUnit

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

const val MMPI_SESSION_TEST_USER_ID = 1L

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MmpiSessionTest {
    private lateinit var testUser: User

    @BeforeAll
    fun init() {
        CentralDataStorage.init(
            rootPath = LaunchMode.TESTS.rootPath,
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
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `just run`() = runBlocking {

        var session: MmpiSession? = null
        val questionsIds = generateSequence(0L) { it + 1 }.iterator()

        session = MmpiSession(
            id = 0,
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
            checkSessionResult(session, it)
        }

        session.start(user = testUser, chatId = 1L)

        val answersIds = generateSequence(0L) { it + 1 }.iterator()

        do {
            val res = session.onCallbackFromUser(answersIds.next(), Gender.Male.name)
            assertTrue(res is Result.Success, "$res")
        } while (res is Result.Error)

        session.testingCallback = { answers ->
            assertEquals(TypeOfTest.Mmpi566.size, answers.size)
            assertTrue(answers.all { it == MmpiProcess.Answer.Agree })
        }

        repeat(TypeOfTest.Mmpi566.size) {
            val id = answersIds.next()
            val res = session.onCallbackFromUser(
                messageId = id,
                data = MmpiProcess.Answer.Agree.name
            )
            assertTrue(res is Result.Success, "$res")
        }
    }

    private fun checkSessionResult(session: MmpiSession?, it: TelegramSession<Any>) = runBlocking {
        assertEquals(session, it)

        val answersResult = CentralDataStorage.usersStorage.getUserAnswers(testUser)
        assertTrue(answersResult is Result.Success)

        val answers = (answersResult as Result.Success).data
        assertFalse(answers.isEmpty())
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `change answers`() = runBlocking {
        var session: MmpiSession? = null
        val questionsIds = generateSequence(0L) { it + 1 }.iterator()

        session = MmpiSession(
            id = 0,
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
        }

        session.start(user = testUser, chatId = 2)
        val answersIds = generateSequence(0L) { it + 1 }.iterator()

        do {
            val res = session.onCallbackFromUser(answersIds.next(), Gender.Male.name)
            assertTrue(res is Result.Success, "$res")
        } while (res is Result.Error)

        repeat(TypeOfTest.Mmpi566.size) {
            sendAnswerToSession(answersIds, session, it)
        }
    }

    private suspend fun sendAnswerToSession(
        answersIds: Iterator<Long>,
        session: MmpiSession,
        it: Int
    ) {
        val id = answersIds.next()
        val res = session.onCallbackFromUser(
            messageId = id,
            data = MmpiProcess.Answer.Agree.name
        )
        assertTrue(res is Result.Success, "$res")

        if (it % 2 == 0) {//edit given answer
            session.onCallbackFromUser(
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