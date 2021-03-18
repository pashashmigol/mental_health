package storage

import lucher.LucherData
import lucher.loadLucherData
import mmpi.MmpiData
import mmpi.storage.loadMmpiData
import models.User
import java.util.*
import java.text.MessageFormat


object CentralDataStorage {
    private lateinit var connection: GoogleDriveConnection

    val lucherData get() = lucher
    val mmpi566Data get() = mmpi566
    val mmpi377Data get() = mmpi377
    val users get() = usersRepository
    val reports get() = reportsRepository

    fun init(rootPath: String) {
        connection = GoogleDriveConnection(rootPath)
    }

    private lateinit var lucher: LucherData
    private lateinit var mmpi566: MmpiData
    private lateinit var mmpi377: MmpiData
    private lateinit var usersRepository: Users
    private lateinit var reportsRepository: Reports

    fun reload() {
        lucher = loadLucherData(connection)

        mmpi566 = loadMmpiData(connection, Settings.MMPI_566_QUESTIONS_FILE_ID)
        mmpi377 = loadMmpiData(connection, Settings.MMPI_377_QUESTIONS_FILE_ID)

        usersRepository = Users(connection.database)
        reportsRepository = Reports(connection)
    }

    private val messages: ResourceBundle = ResourceBundle.getBundle("Messages")
    private val locale = Locale("ru", "ru")
    fun string(key: String, vararg parameters: Any): String {
        return MessageFormat(messages.getString(key), locale).format(parameters)
    }

    fun string(key: String): String {
        return messages.getString(key)
    }

    fun createUser(userId: Long, userName: String) {
        val (_, reportsFolderLink) = reportsRepository.createFolder(userId.toString())

        val user = User(
            id = userId,
            name = userName,
            googleDriveFolder = reportsFolderLink

        )
        usersRepository.add(user)
    }
}