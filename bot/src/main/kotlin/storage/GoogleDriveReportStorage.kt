package storage

import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.model.File
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import models.TypeOfTest
import java.io.ByteArrayInputStream
import java.io.InputStream
import Result
import Settings.ROOT_DIRECTORY_ID
import Settings.TEST_ROOT_DIRECTORY_ID
import com.google.api.services.drive.model.Permission
import models.User


class GoogleDriveReportStorage(
    private val connection: GoogleDriveConnection,
    private val testingMode: Boolean
) : ReportStorage {

    override fun saveLucher(
        user: User,
        bytes: ByteArray,
    ): Result<Folder> {
        val date = DateTime.now().format(DateFormat.DEFAULT_FORMAT)
        val fileName = R.string("lusher_result_filename", date)

        val folderResult = saveFile(
            fileName = fileName,
            user = user,
            contentStream = ByteArrayInputStream(bytes)
        )
        println("saveLucher(); report saved to : $folderResult")
        return folderResult
    }

    override fun saveMmpi(
        user: User,
        bytes: ByteArray,
        typeOfTest: TypeOfTest
    ): Result<Folder> {
        val date = DateTime.now().format(DateFormat.DEFAULT_FORMAT)

        val fileName = when (typeOfTest) {
            TypeOfTest.Mmpi566 -> R.string("mmpi_566_result_filename", date)
            TypeOfTest.Mmpi377 -> R.string("mmpi_377_result_filename", date)
            else -> throw IllegalStateException()
        }

        val folderResult = saveFile(
            fileName = fileName,
            user = user,
            contentStream = ByteArrayInputStream(bytes)
        )
        println("saveMmpi(); report saved to : $folderResult")

        return folderResult
    }

    private fun saveFile(
        fileName: String,
        user: User,
        contentStream: InputStream
    ): Result<Folder> = try {

        val userFolder = Folder(
            id = user.googleDriveFolderId,
            link = user.googleDriveFolderUrl
        )
        val fileLink = createFile(
            name = fileName,
            folderId = userFolder.id,
            contentStream = contentStream
        )
        giveAccess(userFolder.id)
        println("fileLink : $fileLink")

        Result.Success(userFolder)

    } catch (e: Exception) {
        println("Exception : $e")
        Result.Error("ReportsStorage.saveFile() ${e.message}", e)
    }

    override fun giveAccess(folderId: String) {
        val permission = Permission()

        val details = Permission.PermissionDetails()
        permission.role = "reader"
        permission.type = "anyone"

        permission.permissionDetails = listOf(details)

        val result: Permission = connection.driveService.permissions()
            .create(folderId, permission)
            .execute()

        println("result : $result")
    }

    override fun createUserFolder(userName: String): Result<Folder> {
        val folderMetadata = File()
        folderMetadata.name = userName
        folderMetadata.mimeType = "application/vnd.google-apps.folder"
        folderMetadata.parents = if (testingMode) listOf(TEST_ROOT_DIRECTORY_ID) else listOf(ROOT_DIRECTORY_ID)

        val folder = connection.driveService.files()
            .create(folderMetadata)
            .setFields("id, webViewLink")
            .execute()

        println("Folder ID: " + folder.id)

        return Result.Success(Folder(folder.id, folder.webViewLink))
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