package storage

import lucher.LucherAnswers
import lucher.LucherData
import lucher.LucherResult
import lucher.loadLucherData
import lucher.report.pdfReportLucher
import mmpi.MmpiAnswers
import mmpi.MmpiData
import mmpi.MmpiProcess
import mmpi.report.pdfReportMmpi
import mmpi.storage.loadMmpiData
import models.Question
import models.TypeOfTest
import models.User
import report.PdfFonts
import java.util.*
import java.text.MessageFormat

typealias Link = String

object CentralDataStorage {
    private lateinit var connection: GoogleDriveConnection
    private lateinit var fonts: PdfFonts

    val lucherData get() = lucher
    val mmpi566Data get() = mmpi566
    val mmpi377Data get() = mmpi377

    val usersStorage get() = _usersStorage
    private val reportsStorage get() = _reportsStorage

    fun init(rootPath: String, testingMode: Boolean = false) {
        if (!this::connection.isInitialized) {
            connection = GoogleDriveConnection(rootPath, testingMode)
            fonts = PdfFonts(rootPath)
            reload()
        }
    }

    private lateinit var lucher: LucherData
    private lateinit var mmpi566: MmpiData
    private lateinit var mmpi377: MmpiData

    private lateinit var _usersStorage: UsersStorage
    private lateinit var _reportsStorage: ReportsStorage

    fun reload() {
        lucher = loadLucherData(connection)

        mmpi566 = loadMmpiData(connection, Settings.MMPI_566_QUESTIONS_FILE_ID)
        mmpi377 = loadMmpiData(connection, Settings.MMPI_377_QUESTIONS_FILE_ID)

        _usersStorage = UsersStorage(connection.database)
        _reportsStorage = ReportsStorage(connection)
    }

    private val messages: ResourceBundle = ResourceBundle.getBundle("Messages")
    private val locale = Locale("ru", "ru")
    fun string(key: String, vararg parameters: Any): String {
        return MessageFormat(messages.getString(key), locale).format(parameters)
    }

    fun pdfFonts() = fonts

    fun string(key: String): String {
        return messages.getString(key)
    }

    fun createUser(userId: Long, userName: String) {
        val (folderId, reportsFolderLink) = _reportsStorage.createFolder(userId.toString())
        giveAccess(folderId, connection)

        val user = User(
            id = userId,
            name = userName,
            googleDriveFolder = reportsFolderLink
        )
        _usersStorage.addUser(user)
    }

    fun saveMmpi(
        user: User,
        typeOfTest: TypeOfTest,
        result: MmpiProcess.Result,
        questions: List<Question>,
        answers: MmpiAnswers,
        saveAnswers: Boolean
    ): Link {
        if (saveAnswers) {
            usersStorage.saveAnswers(answers)
        }
        val pdfStr = pdfReportMmpi(
            questions = questions,
            answers = answers,
            result = result,
        )
        return reportsStorage.saveMmpi(user.id, pdfStr, typeOfTest)
    }

    fun saveLucher(
        user: User,
        answers: LucherAnswers,
        result: LucherResult,
        saveAnswers: Boolean
    ): String {
        if (saveAnswers) {
            usersStorage.saveAnswers(answers)
        }

        val bytes = pdfReportLucher(
            answers = answers,
            result = result
        )
        return reportsStorage.saveLucher(user.id, bytes)
    }
}