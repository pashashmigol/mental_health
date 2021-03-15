package storage

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import lucher.LucherAnswers
import lucher.LucherData
import lucher.LucherResult
import lucher.loadLucherData
import lucher.report.generateReport
import mmpi.MmpiData
import mmpi.storage.loadMmpiData
import java.util.*
import java.text.MessageFormat


object CentralDataStorage {
    private lateinit var connection: GoogleDriveConnection

    val lucherData get() = lucher
    val mmpi566Data get() = mmpi566
    val mmpi377Data get() = mmpi377

    fun init(rootPath: String) {
        connection = GoogleDriveConnection(rootPath)
    }

    private lateinit var lucher: LucherData
    private lateinit var mmpi566: MmpiData
    private lateinit var mmpi377: MmpiData

    fun reload() {
        lucher = loadLucherData(connection)
        mmpi566 = loadMmpiData(connection, Settings.MMPI_566_QUESTIONS_FILE_ID)
        mmpi377 = loadMmpiData(connection, Settings.MMPI_377_QUESTIONS_FILE_ID)
    }

    private val messages: ResourceBundle = ResourceBundle.getBundle("Messages")
    private val locale = Locale("ru", "ru")
    fun string(key: String, vararg parameters: Any): String {
        return MessageFormat(messages.getString(key), locale).format(parameters)
    }

    fun string(key: String): String {
        return messages.getString(key)
    }

    fun saveLucher(
        userId: String,
        answers: LucherAnswers,
        result: LucherResult
    ): String {
        val date = DateTime.now().format(DateFormat.DEFAULT_FORMAT)
        val fileName = string("lusher_result_filename", date)

        val report = generateReport(userId, answers, result)

        val parentFolderLink = connection.saveFile(
            fileName = fileName,
            folderName = userId,
            textContent = report
        )
        println("saveLucher(); report saved to : $parentFolderLink")
        return parentFolderLink
    }

    fun saveMmpi(
        userId: String,
        report: String
    ): String {
        val fileName = "MMPI ${DateTime.now().format(DateFormat.DEFAULT_FORMAT)}.html"

        val parentFolderLink = connection.saveFile(
            fileName = fileName,
            folderName = userId,
            textContent = report
        )
        println("saveMmpi(); report saved to : $parentFolderLink")
        return parentFolderLink
    }
}