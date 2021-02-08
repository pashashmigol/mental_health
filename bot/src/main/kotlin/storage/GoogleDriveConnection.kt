package storage

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream

class GoogleDriveConnection(projectRoot: String) {

    companion object {
        private const val CREDENTIALS_FILE_NAME = "mental-health-300314-1be17f2cdb6f.json"
    }

    private val serviceAccount = FileInputStream("$projectRoot$CREDENTIALS_FILE_NAME")
    private val credentials: GoogleCredentials = GoogleCredentials.fromStream(serviceAccount)


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