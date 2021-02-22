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

object CentralDataStorage {
    private lateinit var connection: GoogleDriveConnection

    val lucherData get() = lucher
    val mmpiData get() = mmpi

    fun init(rootPath: String) {
        connection = GoogleDriveConnection(rootPath)
    }

    private lateinit var lucher: LucherData
    private lateinit var mmpi: MmpiData
    fun reload() {
        lucher = loadLucherData(connection)
        mmpi = loadMmpiData(connection)
    }

    fun saveLucher(
        userId: String,
        answers: LucherAnswers,
        result: LucherResult
    ): String {
        val fileName = "Люшер ${DateTime.now().format(DateFormat.DEFAULT_FORMAT)}.html"
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