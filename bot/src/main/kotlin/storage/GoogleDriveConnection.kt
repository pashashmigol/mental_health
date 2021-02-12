package storage

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.testing.http.MockHttpContent
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream
import java.io.InputStream
import java.util.Collections


class GoogleDriveConnection(projectRoot: String) {

    companion object {
        private const val CREDENTIALS_FILE_NAME = "mental-health-300314-1be17f2cdb6f.json"
    }

    private val serviceAccount = FileInputStream("$projectRoot$CREDENTIALS_FILE_NAME")
    private val credentials: GoogleCredentials = GoogleCredentials.fromStream(serviceAccount)

    fun saveReport(
//        userId: String,
//        testName: String,
//        title: String,
//        text: String
    ) {

        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val jacksonFactory = JacksonFactory.getDefaultInstance()
        val scopes = listOf(SheetsScopes.DRIVE)

        val driveService = com.google.api.services.drive.Drive.Builder(
            transport,
            jacksonFactory,
            HttpCredentialsAdapter(credentials.createScoped(scopes))
        )
            .setApplicationName("imaginary_friend")
            .build()

        try {
            val folderMetadata = File()
            folderMetadata.name = "Invoices"
            folderMetadata.mimeType = "application/vnd.google-apps.folder"

            val permission = createPermission()
//            folderMetadata.permissions = listOf(permission)

            val folder = driveService.files()
                .create(folderMetadata)
                .setFields("id")
                .execute()

            println("Folder ID: " + folder.id)

            val folderId = folder.id
            val fileMetadata = File()
            fileMetadata.name = "photo.jpg"
            fileMetadata.parents = listOf(folderId)
//            fileMetadata.permissions = listOf(permission)

            val mediaContent = InputStreamContent(null, InputStream.nullInputStream())

            val file = driveService.files()
                .create(fileMetadata, mediaContent)
                .setFields("id, parents")
                .execute()

            println("File ID: " + file.id)

            val result = driveService.permissions()
                .create(folderId, createPermission())
                .execute()


            println("result : $result")

        } catch (e: Exception) {
            println("Exception : $e")
        }
    }

    private fun createPermission(): Permission {
        val permission = Permission()

        permission.emailAddress = "pashashmigol@gmail.com"

        val details = Permission.PermissionDetails()
        permission.role = "writer"
        permission.type = "user"

        permission.permissionDetails = listOf(details)

        return permission
    }

    fun loadDataFromFile(fileId: String, page: String): List<Map<String, String>> {
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val jacksonFactory = JacksonFactory.getDefaultInstance()

        val scopes = listOf(SheetsScopes.SPREADSHEETS_READONLY)
        val local: HttpRequestInitializer by lazy {
            HttpCredentialsAdapter(credentials.createScoped(scopes))
        }
        val sheets = Sheets.Builder(transport, jacksonFactory, local).build()

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