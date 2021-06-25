package storage

import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import models.TypeOfTest
import java.io.ByteArrayInputStream
import java.io.InputStream
import Result


class ReportsStorage(private val connection: GoogleDriveConnection) {

    fun saveLucher(
        userId: Long,
        bytes: ByteArray,
    ): Result<Link> {
        val date = DateTime.now().format(DateFormat.DEFAULT_FORMAT)
        val fileName = CentralDataStorage.string("lusher_result_filename", date)

        val folderLink = saveFile(
            fileName = fileName,
            folderName = userId.toString(),
            contentStream = ByteArrayInputStream(bytes)
        )
        println("saveLucher(); report saved to : $folderLink")
        return folderLink
    }

    fun saveMmpi(
        userId: Long,
        bytes: ByteArray,
        typeOfTest: TypeOfTest
    ): Result<Link> {
        val date = DateTime.now().format(DateFormat.DEFAULT_FORMAT)

        val fileName = when (typeOfTest) {
            TypeOfTest.Mmpi566 -> CentralDataStorage.string("mmpi_566_result_filename", date)
            TypeOfTest.Mmpi377 -> CentralDataStorage.string("mmpi_377_result_filename", date)
            else -> throw IllegalStateException()
        }

        val folderLink = saveFile(
            fileName = fileName,
            folderName = userId.toString(),
            contentStream = ByteArrayInputStream(bytes)
        )
        println("saveMmpi(); report saved to : $folderLink")
        return folderLink
    }

    private fun saveFile(
        fileName: String,
        folderName: String,
        contentStream: InputStream
    ): Result<Link> = try {
        val (folderId, _) = findFolder(folderName) ?: createFolder(folderName)
        val fileLink = createFile(fileName, folderId, contentStream)

        giveAccess(folderId, connection)
        println("fileLink : $fileLink")

        Result.Success(fileLink)

    } catch (e: Exception) {
        println("Exception : $e")
        Result.Error("ReportsStorage.saveFile()", e)
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

    private fun createFile(name: String, folderId: String, contentStream: InputStream): Link {
        val fileMetadata = File()

        fileMetadata.name = name
        fileMetadata.parents = listOf(folderId)

        val mediaContent = InputStreamContent(
            "application/pdf",
            contentStream
        )

        val file = connection.driveService.files()
            .create(fileMetadata, mediaContent)
            .setFields("id, parents, webContentLink")
            .execute()

        return file.webContentLink
    }
}