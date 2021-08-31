package mmpi.telegram

import kotlinx.coroutines.runBlocking
import models.TypeOfTest

import Result
import mmpi.MmpiAnswersContainer
import mmpi.MmpiData
import mmpi.MmpiProcess
import models.AnswersContainer
import models.User
import models.size
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.kodein.di.instance
import storage.GoogleDriveReportStorage
import storage.users.*
import telegram.*
import testDI
import testStoragePack
import java.util.concurrent.TimeUnit

const val MMPI_SESSION_TEST_USER_ID = 555L

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MmpiSessionTest {
    private lateinit var testUser: User

    private val userStorage: UserStorage by testDI.instance()
    private val reportStorage: GoogleDriveReportStorage by testDI.instance()
    private val mmpi566Data: MmpiData by testDI.instance(TypeOfTest.Mmpi566)
    private val sessionsStorage: SessionStorage by testDI.instance()
    private val answerStorage: AnswerStorage by testDI.instance()

    @BeforeAll
    fun init() = runBlocking {
        createUser(
            MMPI_SESSION_TEST_USER_ID,
            "MmpiSessionTest User",
            reportStorage = reportStorage,
            userStorage = userStorage
        )
        testUser = userStorage.getUser(MMPI_SESSION_TEST_USER_ID)!!
    }

    @AfterAll
    fun cleanUp() = runBlocking {
        userStorage.clear()
        sessionsStorage.clear()
        answerStorage.clear()
    }

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    fun `basic case`() = runBlocking {

        var session: MmpiSession? = null
        val questionsIds = generateSequence(0L) { it + 1 }.iterator()

        session = MmpiSession(
            user = testUser,
            chatId = 1L,
            roomId = 0L,
            type = TypeOfTest.Mmpi566,
            userConnection = stubUserConnection(questionsIds),
            storagePack = testStoragePack,
            mmpiData = mmpi566Data,
        ) {
            checkSessionResult(testUser, session, it, answerStorage = answerStorage)
        }
        session.start()

        val res = session.sendAnswer(
            userAnswer = UserAnswer.GenderAnswer(
                answer = Gender.Male
            ),
            messageId = NOT_SENT
        )
        assertTrue(res is Result.Success, "$res")

        session.testingCallback = { answers ->
            assertEquals(TypeOfTest.Mmpi566.size, answers.size)
            assertTrue(answers.all { it == MmpiProcess.Answer.Agree })
            checkState(
                answers = answers,
                session = session,
                mmpiData = mmpi566Data,
            )
        }

        repeat(TypeOfTest.Mmpi566.size) {
            val answerResult = session.sendAnswer(
                userAnswer = UserAnswer.Mmpi(
                    index = it,
                    answer = MmpiProcess.Answer.Agree
                ),
                messageId = NOT_SENT
            )
            assertTrue(answerResult is Result.Success, "$answerResult")
        }
    }

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    fun `change answers`() = runBlocking {
        var session: MmpiSession? = null
        val questionsIds = generateSequence(0L) { it + 1 }.iterator()

        session = MmpiSession(
            user = testUser,
            chatId = 2L,
            roomId = 0L,
            type = TypeOfTest.Mmpi566,
            userConnection = stubUserConnection(questionsIds),
            storagePack = testStoragePack,
            mmpiData = mmpi566Data,
        ) {
            assertEquals(session, it)
        }
        session.testingCallback = { answers ->
            checkEditedAnswers(answers)
            checkState(
                answers = answers,
                session = session,
                mmpiData = mmpi566Data
            )
        }

        session.start()
        val answersIds = generateSequence(0) { it + 1 }.iterator()

        do {
            val res = session.sendAnswer(
                userAnswer = UserAnswer.GenderAnswer(
                    answer = Gender.Male
                ),
                messageId = NOT_SENT
            )
            assertTrue(res is Result.Success, "$res")
        } while (res is Result.Error)

        repeat(TypeOfTest.Mmpi566.size) {
            sendAnswersToSession(answersIds, session, it)
        }
    }

    private suspend fun sendAnswersToSession(
        answersIds: Iterator<Int>,
        session: MmpiSession,
        it: Int
    ) {
        val index = answersIds.next()

        val res = session.sendAnswer(
            userAnswer = UserAnswer.Mmpi(
                index = index,
                answer = MmpiProcess.Answer.Agree
            ),
            messageId = NOT_SENT
        )
        assertTrue(res is Result.Success, "$res")

        if (it % 2 == 0) {//edit given answer
            session.sendAnswer(
                userAnswer = UserAnswer.Mmpi(
                    index = index,
                    answer = MmpiProcess.Answer.Disagree
                ), messageId = NOT_SENT
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

        val answersResult = answerStorage.getUserAnswers(testUser)
        assertTrue(answersResult is Result.Success)

        val answersContainerFromDatabase: List<AnswersContainer> = (answersResult as Result.Success).data
        assertFalse(answersContainerFromDatabase.isEmpty())

        val lastAvailableAnswers = (answersContainerFromDatabase.first() as MmpiAnswersContainer).answersList

        assertArrayEquals(answers.toTypedArray(), lastAvailableAnswers.toTypedArray())

        answers.zip(lastAvailableAnswers).forEach {
            assertEquals(it.first, it.second)
        }
    }
}

private fun checkState(
    answers: List<MmpiProcess.Answer>,
    session: MmpiSession?,
    mmpiData: MmpiData
) = runBlocking {
    val sessionState = session!!.state

    assertEquals(session.roomId, sessionState.roomId)
    assertEquals(session.sessionId, sessionState.sessionId)
    assertEquals(session.type, TypeOfTest.Mmpi566)

    val restoredSession = MmpiSession(
        user = User(
            id = 0,
            name = "",
            googleDriveFolderUrl = "",
            googleDriveFolderId = "",
            runDailyQuiz = false
        ),
        chatId = 1L,
        roomId = sessionState.sessionId,
        type = sessionState.type,
        userConnection = object : UserConnection {},
        storagePack = testStoragePack,
        mmpiData = mmpiData,
        onEndedCallback = {}
    )

    restoredSession.testingCallback = {
        assertEquals(answers, it)
    }

    restoredSession.applyState(sessionState)
}

private fun checkSessionResult(
    user: User, session: MmpiSession?,
    telegramSession: TelegramSession<Any>,
    answerStorage: AnswerStorage
) = runBlocking {
    assertEquals(session, telegramSession)

    val answersResult = answerStorage.getUserAnswers(user)
    assertTrue(answersResult is Result.Success)

    val answers = (answersResult as Result.Success).data
    assertFalse(answers.isEmpty())
}

private fun stubUserConnection(questionsIds: Iterator<Long>) = object : UserConnection {

    override fun sendMessageWithButtons(
        chatId: Long,
        text: String,
        buttons: List<Button>,
        placeButtonsVertically: Boolean
    ): Long {
        return questionsIds.next()
    }

    override fun notifyAdmin(text: String, exception: Throwable?): MessageId {
        exception?.apply { throw this }
        return NOT_SENT
    }
}