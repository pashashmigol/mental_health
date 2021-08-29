package telegram

import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import lucher.LucherData
import mmpi.MmpiData
import models.TypeOfTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.kodein.di.instance
import quiz.DailyQuizData
import quiz.DailyQuizSession
import storage.ReportStorage
import storage.users.UserStorage
import testDI
import java.util.concurrent.TimeUnit

@InternalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TelegramRoomTest {

    private val userStorage: UserStorage by testDI.instance()
    private val reportStorage: ReportStorage by testDI.instance()


    private val mmpi566Data: MmpiData by testDI.instance(TypeOfTest.Mmpi566)
    private val mmpi377Data: MmpiData by testDI.instance(TypeOfTest.Mmpi377)
    private val lucherData: LucherData by testDI.instance()
    private val dailyQuizData: DailyQuizData by testDI.instance()

    @BeforeAll
    fun setup() {
        userStorage.clear()
    }

    @AfterAll
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    fun clear() {
        userStorage.clear()
    }

    @Test
    fun `sessions saving`() = runBlocking {
        val originalRoom = TelegramRoom(
            roomId = 0,
            userConnection = MockUserConnection,
            reportStorage = reportStorage,
            userStorage = userStorage,
            lusherData = lucherData,
            mmpiData566 = mmpi566Data,
            mmpiData377 = mmpi377Data,
            dailyQuizData = dailyQuizData,
        )
        for (id in 0..36L step 4) {
            originalRoom.launchMmpi377(
                launchSession(id, originalRoom)
            ).join()
            originalRoom.launchMmpi566(
                launchSession(id + 1, originalRoom)
            ).join()
            originalRoom.launchLucher(
                launchSession(id + 2, originalRoom)
            ).join()
            originalRoom.launchDailyQuiz(
                launchSession(id + 3, originalRoom),
                DailyQuizSession.Time.MORNING
            ).join()
        }

        assertEquals(40, originalRoom.sessions.size)

        val restoredRoom = TelegramRoom(
            roomId = 0,
            userConnection = MockUserConnection,
            reportStorage = reportStorage,
            userStorage = userStorage,
            lusherData = lucherData,
            mmpiData566 = mmpi566Data,
            mmpiData377 = mmpi377Data,
            dailyQuizData = dailyQuizData
        )

        restoredRoom.restoreState()
        assertEquals(40, restoredRoom.sessions.size)
    }
}

object MockUserConnection : UserConnection

@InternalAPI
private suspend fun launchSession(id: Long, originalRoom: TelegramRoom): ChatInfo {
    val chatInfo = ChatInfo(
        userId = id,
        userName = "user $id",
        chatId = id,
        messageId = id
    )
    originalRoom.welcomeUser(
        chatInfo = chatInfo,
        userConnection = MockUserConnection
    ).join()

    return chatInfo
}