import Settings.QUESTIONS_FILE_ID_GOOGLE_DOC
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream


interface QuestionsProvider {
    val mockTestQuestions: Array<Question>
}

object CurrentQuestionsProvider : QuestionsProvider {
    private var internalProvider: QuestionsProvider? = null

    fun initGoogleSheetsProvider(rootPath: String) {
        internalProvider = GoogleSheetsQuestionsProvider(rootPath)
    }

    override val mockTestQuestions
        get() = internalProvider?.mockTestQuestions ?: emptyArray()
}

class GoogleSheetsQuestionsProvider(projectRoot: String) : QuestionsProvider {
    companion object {
        private const val CREDENTIALS_FILE_NAME = "mental-health-300314-1be17f2cdb6f.json"
    }

    private val serviceAccount = FileInputStream("$projectRoot$CREDENTIALS_FILE_NAME")
    private val credentials: GoogleCredentials = GoogleCredentials.fromStream(serviceAccount)

    private var _allQuestions: Array<Question> = emptyArray()
    private var answerOptions: Array<String> = emptyArray()

    override val mockTestQuestions: Array<Question>
        get() = _allQuestions

    init {
        loadQuestions()
    }

    fun loadQuestions() {
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val jacksonFactory = JacksonFactory.getDefaultInstance()
        val scopes = listOf(SheetsScopes.SPREADSHEETS_READONLY)
        val local: HttpRequestInitializer by lazy {
            HttpCredentialsAdapter(credentials.createScoped(scopes))
        }

        val sheets = Sheets.Builder(transport, jacksonFactory, local).build()

        val answerOptionsRequest = sheets.spreadsheets()
            .values().get(QUESTIONS_FILE_ID_GOOGLE_DOC, "'answer_options'")

        answerOptions = answerOptionsRequest.execute().getValues()
            .toRawEntries()
            .map { it["answer"] as String }
            .toTypedArray()

        val allSheetsRequest = sheets.spreadsheets()
            .values().get(QUESTIONS_FILE_ID_GOOGLE_DOC, "'questions'")
        _allQuestions = allSheetsRequest.execute().getValues()
            .toRawEntries()
            .map { it.toQuestion(answerOptions) }
            .toTypedArray()
    }
}

private fun Map<String, Any>.toQuestion(answerOptions: Array<String>) = Question(
    text = stringFor("question"),
    options = answerOptions
)

private fun List<List<Any>>.toRawEntries(): List<Map<String, Any>> {
    val headers = first().map { it as String }
    val rows = drop(1)
    return rows.map { row -> headers.zip(row).toMap() }
}

private fun Map<String, Any>.stringFor(key: String) = (this[key] as? String) ?: ""