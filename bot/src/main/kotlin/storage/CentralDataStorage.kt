package storage

import com.soywiz.klock.DateTime
import lucher.LucherAnswers
import lucher.LucherData
import lucher.LucherResult
import lucher.loadLucherData
import mmpi.MmpiAnswers
import mmpi.MmpiData
import mmpi.MmpiProcess
import mmpi.report.generateReport
import mmpi.storage.loadMmpiData
import models.Question
import models.Type
import models.User
import java.util.*
import java.text.MessageFormat


object CentralDataStorage {
    private lateinit var connection: GoogleDriveConnection

    val lucherData get() = lucher
    val mmpi566Data get() = mmpi566
    val mmpi377Data get() = mmpi377
    val users get() = usersRepository
    private val reports get() = reportsRepository

    fun init(rootPath: String, testingMode: Boolean = false) {
        if (!this::connection.isInitialized) {
            connection = GoogleDriveConnection(rootPath, testingMode)
            reload()
        }
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
        val (folderId, reportsFolderLink) = reportsRepository.createFolder(userId.toString())
        giveAccess(folderId, connection)

        val user = User(
            id = userId,
            name = userName,
            googleDriveFolder = reportsFolderLink
        )
        usersRepository.add(user)
    }

    fun saveMmpi(
        user: User,
        type: Type,
        questions: List<Question>,
        answers: List<MmpiProcess.Answer>,
        result: MmpiProcess.Result
    ): String {
        val mmpiAnswers = MmpiAnswers(
            user = user,
            dateTime = DateTime.now(),
            data = answers
        )
        users.saveAnswers(user, mmpiAnswers)

        val report = generateReport(
            user = user,
            questions = questions,
            answers = answers,
            result = result
        )
        return reports.saveMmpi(user.id, report, type)
    }

    fun saveLucher(
        user: User,
        answers: LucherAnswers,
        result: LucherResult
    ): String {

        users.saveAnswers(user, answers)

        return reports.saveLucher(
            user = user,
            answers = answers,
            result = result
        )
    }
}