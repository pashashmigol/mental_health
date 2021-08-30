package telegram

import DataPack
import org.junit.jupiter.api.*
import storage.users.UserStorage
import io.ktor.util.*
import storage.ReportStorage

@InternalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TelegramRoomUnitTest {


    @Test
    fun onMorningChron() {
        val userConnection = object : UserConnection {}
        val userStorage = object : UserStorage {}
        val reportStorage = object : ReportStorage {}
        val dataPack = object : DataPack {}

        val telegramRoom = TelegramRoom(
            roomId = 0,
            userConnection = userConnection,
            reportStorage = reportStorage,
            userStorage = userStorage,
            dataPack = dataPack
        )

        Assertions.assertNotNull(telegramRoom)
    }

    @Test
    fun onEveningChron() {
    }

    @Test
    fun launchDailyQuiz() {
    }
}