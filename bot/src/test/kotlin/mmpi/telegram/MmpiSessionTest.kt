package mmpi.telegram

import kotlinx.coroutines.runBlocking
import models.Type
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout

import storage.CentralDataStorage
import telegram.LaunchMode
import java.util.concurrent.TimeUnit

import Result
import mmpi.MmpiProcess
import models.User
import models.size
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import telegram.Button
import telegram.UserConnection


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MmpiSessionTest {

    private lateinit var user: User

    @BeforeAll
    fun init() {
        CentralDataStorage.init(
            rootPath = LaunchMode.TESTS.rootPath,
            testingMode = true
        )

        val userId = 1L
        CentralDataStorage.createUser(userId, "test_user")
        user = CentralDataStorage.users.get(userId)!!
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun `just run`() = runBlocking {
        println("### just run()")

        var session: MmpiSession? = null
        val questionsIds = generateSequence(0L) { it + 1 }.iterator()

        session = MmpiSession(
            id = 0,
            type = Type.Mmpi566,
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

        session.start(user = user, chatId = 1L)

        val answersIds = generateSequence(0L) { it + 1 }.iterator()

        do {
            val res = session.onCallbackFromUser(answersIds.next(), Gender.Male.name)
            assertTrue(res is Result.Success, "$res")
        } while (res is Result.Error)

        session.testingCallback = { answers ->
            assertEquals(Type.Mmpi566.size, answers.size)
            assertTrue(answers.all { it == MmpiProcess.Answer.Agree })
        }

        repeat(Type.Mmpi566.size) {
            val id = answersIds.next()
            val res = session.onCallbackFromUser(
                messageId = id,
                data = MmpiProcess.Answer.Agree.name
            )
            assertTrue(res is Result.Success, "$res")
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `change answers`() = runBlocking {
        println("### change answers()")

        var session: MmpiSession? = null
        val questionsIds = generateSequence(0L) { it + 1 }.iterator()

        session = MmpiSession(
            id = 0,
            type = Type.Mmpi566,
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
            assertEquals(Type.Mmpi566.size, answers.size)

            answers.forEachIndexed { i, answer ->
                if (i % 2 == 0) {
                    assertEquals(MmpiProcess.Answer.Disagree, answer, "i = $i")
                } else {
                    assertEquals(MmpiProcess.Answer.Agree, answer, "i = $i")
                }
            }
        }

        session.start(user = user, chatId = 2)
        val answersIds = generateSequence(0L) { it + 1 }.iterator()

        do {
            val res = session.onCallbackFromUser(answersIds.next(), Gender.Male.name)
            assertTrue(res is Result.Success, "$res")
        } while (res is Result.Error)

        repeat(Type.Mmpi566.size) {
            val id = answersIds.next()
            val res = session.onCallbackFromUser(
                messageId = id,
                data = MmpiProcess.Answer.Agree.name
            )
            assertTrue(res is Result.Success, "$res")

            if (it % 2 == 0) {
                session.onCallbackFromUser(
                    messageId = id,
                    data = MmpiProcess.Answer.Disagree.name
                )
            }
        }
    }
}