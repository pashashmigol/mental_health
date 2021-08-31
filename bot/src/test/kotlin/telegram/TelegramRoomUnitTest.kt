package telegram

import DataPack
import StoragePack
import org.junit.jupiter.api.*
import storage.users.UserStorage
import io.ktor.util.*
import models.User


@InternalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TelegramRoomUnitTest {

    @Test
    fun onMorningChron() {
        val userConnection = object : UserConnection {

        }
        val userStorage = object : UserStorage {
            override fun allUsers(): List<User> {
                return listOf(
                    User(id = 0, name = "0", googleDriveFolderUrl = "", googleDriveFolderId = "", runDailyQuiz = true),
                    User(id = 1, name = "1", googleDriveFolderUrl = "", googleDriveFolderId = "", runDailyQuiz = true),
                    User(id = 2, name = "2", googleDriveFolderUrl = "", googleDriveFolderId = "", runDailyQuiz = false)
                )
            }
        }
        val storagePack = object : StoragePack {
            override val userStorage = userStorage
        }
        val dataPack = object : DataPack {}

        val telegramRoom = TelegramRoom(
            roomId = 0,
            userConnection = userConnection,
            dataPack = dataPack,
            storagePack = storagePack
        )

        Assertions.assertNotNull(telegramRoom)
        telegramRoom.onMorningChron()
    }

    @Test
    fun onEveningChron() {
    }

    @Test
    fun launchDailyQuiz() {
    }
}