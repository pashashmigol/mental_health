package storage

import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import lucher.LucherAnswers
import lucher.LucherResult
import lucher.report.generateReport
import models.Type

class Reports(private val connection: GoogleDriveConnection) {

    fun saveLucher(
        userId: String,
        answers: LucherAnswers,
        result: LucherResult
    ): String {
        val date = DateTime.now().format(DateFormat.DEFAULT_FORMAT)
        val fileName = CentralDataStorage.string("lusher_result_filename", date)

        val report = generateReport(userId, answers, result)

        val parentFolderLink = saveFile(
            fileName = fileName,
            folderName = userId,
            textContent = report
        )
        println("saveLucher(); report saved to : $parentFolderLink")
        return parentFolderLink
    }

    fun saveMmpi(
        userId: String,
        report: String,
        type: Type
    ): String {
        val date = DateTime.now().format(DateFormat.DEFAULT_FORMAT)
        val fileName = when (type) {
            Type.Mmpi566 -> CentralDataStorage.string("mmpi_566_result_filename", date)
            Type.Mmpi377 -> CentralDataStorage.string("mmpi_377_result_filename", date)
            else -> throw IllegalStateException()
        }

        val parentFolderLink = saveFile(
            fileName = fileName,
            folderName = userId,
            textContent = report
        )
        println("saveMmpi(); report saved to : $parentFolderLink")
        return parentFolderLink
    }

    private fun saveFile(
        fileName: String,
        folderName: String,
        textContent: String
    ): String = try {
        val (folderId, _) = findFolder(folderName) ?: createFolder(folderName)
        val fileLink = createFile(fileName, folderId, textContent)

        giveAccess(folderId, connection)
        println("fileLink : $fileLink")

        fileLink

    } catch (e: Exception) {
        println("Exception : $e")
        ""
    }

    private fun findFolder(name: String): Pair<String, String>? {
        val fileList: FileList = connection.driveService.files().list()
            .setQ("mimeType = 'application/vnd.google-apps.folder' and name = '$name'")
            .setSpaces("drive")
            .setFields("nextPageToken, files(id, name, webViewLink)")
            .execute()

        if (fileList.files.isEmpty()) return null

        val folder = fileList.files.first()
        return Pair(folder.id, folder.webViewLink)
    }

    fun createFolder(name: String): Pair<String, String> {
        val folderMetadata = File()
        folderMetadata.name = name
        folderMetadata.mimeType = "application/vnd.google-apps.folder"

        val folder = connection.driveService.files()
            .create(folderMetadata)
            .setFields("id, webViewLink")
            .execute()

        println("Folder ID: " + folder.id)

        return Pair(folder.id, folder.webViewLink)
    }

    private fun createFile(name: String, folderId: String, content: String): String {
        val fileMetadata = File()

        fileMetadata.name = name
        fileMetadata.parents = listOf(folderId)

        val mediaContent = InputStreamContent(
            "text/html",
            content.byteInputStream()
        )

        val file = connection.driveService.files()
            .create(fileMetadata, mediaContent)
            .setFields("id, parents, webContentLink")
            .execute()

        return file.webContentLink
    }
}