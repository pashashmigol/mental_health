package storage

import Settings.FIREBASE_CREDENTIALS_FILE_NAME
import Settings.FIREBASE_DATABASE_URL
import Settings.FIREBASE_TEST_DATABASE_URL
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream
import com.google.firebase.FirebaseApp

import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import telegram.LaunchMode


class GoogleDriveConnection(launchMode: LaunchMode, private val testingMode: Boolean) {

    val driveService: Drive
    private val sheets: Sheets
    internal val database: FirebaseDatabase

    init {
        val credentialsPath = when(launchMode){
            LaunchMode.TESTS, LaunchMode.LOCAL -> "${launchMode.rootPath}webapp/$FIREBASE_CREDENTIALS_FILE_NAME"
            LaunchMode.APP_ENGINE -> "${launchMode.rootPath}$FIREBASE_CREDENTIALS_FILE_NAME"
        }
        val serviceAccount = FileInputStream(credentialsPath)
        val credentials: GoogleCredentials = GoogleCredentials.fromStream(serviceAccount)
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val jacksonFactory = JacksonFactory.getDefaultInstance()
        val scopes = listOf(SheetsScopes.DRIVE)
        val local = HttpCredentialsAdapter(credentials.createScoped(scopes))

        driveService = Drive.Builder(transport, jacksonFactory, local)
            .setApplicationName("imaginary_friend")
            .build()

        sheets = Sheets.Builder(transport, jacksonFactory, local).build()
        database = initFireBaseDatabase(credentials)

        println("Folder ID: $database")
    }

    private fun initFireBaseDatabase(credentials: GoogleCredentials): FirebaseDatabase {
        val auth = mapOf("uid" to "my-service-worker")
        val databaseUrl = if (testingMode) FIREBASE_TEST_DATABASE_URL else FIREBASE_DATABASE_URL

        val options: FirebaseOptions = FirebaseOptions.builder()
            .setCredentials(credentials)
            .setDatabaseUrl(databaseUrl)
            .setDatabaseAuthVariableOverride(auth)
            .build()

        try {
            FirebaseApp.initializeApp(options)
        } catch (e: Throwable) {
            println("FirebaseApp.initializeApp(): $e")
        }

        return FirebaseDatabase.getInstance()
    }

    fun loadDataFromFile(fileId: String, page: String): List<Map<String, String>>? {
        val request = sheets.spreadsheets()
            .values().get(fileId, page)

        return try {
            request.execute().getValues().toRawEntries()
        } catch (e: Throwable) {
            println("GoogleDriveConnection.loadDataFromFile(): $e")
            null
        }
    }
}