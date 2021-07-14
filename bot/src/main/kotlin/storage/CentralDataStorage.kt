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
import Result
import telegram.LaunchMode

typealias Link = String

data class Folder(val id: String, val link: String)

object CentralDataStorage {
    private lateinit var connection: GoogleDriveConnection
    private lateinit var fonts: PdfFonts

    val lucherData get() = lucher
    val mmpi566Data get() = mmpi566
    val mmpi377Data get() = mmpi377

    val usersStorage get() = _usersStorage
    private val reportsStorage get() = _reportsStorage

    fun init(launchMode: LaunchMode, testingMode: Boolean = false) {
        if (!this::connection.isInitialized) {
            connection = GoogleDriveConnection(launchMode, testingMode)
            fonts = PdfFonts(launchMode)
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

    suspend fun createUser(userId: Long, userName: String) : Result<Unit> {
        val folder = (_reportsStorage.createUserFolder(userId.toString()) as Result.Success<Folder>).data

        giveAccess(folder.id, connection)

        val user = User(
            id = userId,
            name = userName,
            googleDriveFolderUrl = folder.link,
            googleDriveFolderId = folder.id
        )
       return _usersStorage.saveUser(user)
    }


    suspend fun deleteUser(user: User) : Result<Unit> {
        deleteFolder(user.googleDriveFolderId, connection)
        return _usersStorage.clearUser(user)
    }

    suspend fun saveMmpi(
        user: User,
        typeOfTest: TypeOfTest,
        result: MmpiProcess.Result,
        questions: List<Question>,
        answers: MmpiAnswers,
        saveAnswers: Boolean
    ): Result<Folder> {
        if (saveAnswers) {
            usersStorage.saveAnswers(answers).dealWithError {
                return it
            }
        }
        val pdfStr = pdfReportMmpi(
            questions = questions,
            answers = answers,
            result = result,
        )
        return reportsStorage.saveMmpi(user, pdfStr, typeOfTest)
    }

    suspend fun saveLucher(
        user: User,
        answers: LucherAnswers,
        result: LucherResult,
        saveAnswers: Boolean
    ): Result<Folder> {
        if (saveAnswers) {
            usersStorage.saveAnswers(answers).dealWithError {
                return it
            }
        }

        val bytes = pdfReportLucher(
            answers = answers,
            result = result
        )
        return reportsStorage.saveLucher(user, bytes)
    }
}