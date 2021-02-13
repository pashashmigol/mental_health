package storage

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import lucher.LucherAnswers
import lucher.LucherData
import lucher.LucherResult
import lucher.loadLucherData
import mmpi.MmpiData
import mmpi.loadMmpiData

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
    ) {
        val fileName = "Lucher ${DateTime.now().format(DateFormat.DEFAULT_FORMAT)}.txt"
        val text = "${answers.description()}\n\n${result.description()}"

        connection.saveFile(
            fileName = fileName,
            folderName = userId,
            textContent = text,
            shareWithEmail = "pashashmigol@gmail.com"
        )
    }
}