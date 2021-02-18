package storage

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream


class GoogleDriveConnection(projectRoot: String) {
    companion object {
        private const val CREDENTIALS_FILE_NAME = "mental-health-300314-1be17f2cdb6f.json"
    }

    private val driveService: Drive
    private val sheets: Sheets

    init {
        val serviceAccount = FileInputStream("$projectRoot$CREDENTIALS_FILE_NAME")
        val credentials: GoogleCredentials = GoogleCredentials.fromStream(serviceAccount)
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val jacksonFactory = JacksonFactory.getDefaultInstance()
        val scopes = listOf(SheetsScopes.DRIVE)
        val local = HttpCredentialsAdapter(credentials.createScoped(scopes))
        driveService = Drive.Builder(transport, jacksonFactory, local)
            .setApplicationName("imaginary_friend")
            .build()
        sheets = Sheets.Builder(transport, jacksonFactory, local).build()
    }

    fun saveFile(
        fileName: String,
        folderName: String,
        textContent: String
    ): String = try {
        val (folderId, folderLink) = findFolder(folderName) ?: createFolder(folderName)
        val fileId = createFile(fileName, folderId, textContent)

        giveAccess(folderId)
        println("fileId : $fileId")

        folderLink

    } catch (e: Exception) {
        println("Exception : $e")
        ""
    }

    private fun findFolder(name: String): Pair<String, String>? {
        val fileList: FileList = driveService.files().list()
            .setQ("mimeType = 'application/vnd.google-apps.folder' and name = '$name'")
            .setSpaces("drive")
            .setFields("nextPageToken, files(id, name, webViewLink)")
            .execute()

        if (fileList.files.isEmpty()) return null

        val folder = fileList.files.first()
        return Pair(folder.id, folder.webViewLink)
    }

    private fun createFolder(name: String): Pair<String, String> {
        val folderMetadata = File()
        folderMetadata.name = name
        folderMetadata.mimeType = "application/vnd.google-apps.folder"

        val folder = driveService.files()
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

        val file = driveService.files()
            .create(fileMetadata, mediaContent)
            .setFields("id, parents")
            .execute()

        file.webViewLink

        return file.id
    }

    private fun giveAccess(folderId: String) {
        val permission = Permission()

        val details = Permission.PermissionDetails()
        permission.role = "reader"
        permission.type = "anyone"

        permission.permissionDetails = listOf(details)

        val result = driveService.permissions()
            .create(folderId, permission)
            .execute()

        println("result : $result")
    }

    fun loadDataFromFile(fileId: String, page: String): List<Map<String, String>> {
        val request = sheets.spreadsheets()
            .values().get(fileId, page)

        return request.execute().getValues().toRawEntries()
    }


    private fun List<List<Any>>.toRawEntries(): List<Map<String, String>> {
        val headers = first().map { it as String }
        val rows = drop(1).map { row ->
            row.map { field ->
                field.toString()
            }
        }
        return rows.map { row ->
            headers.zip(row).toMap()
        }
    }
}