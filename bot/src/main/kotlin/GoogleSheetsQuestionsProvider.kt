import Settings.QUESTIONS_FILE_ID_GOOGLE_DOC
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter


interface QuestionProvider {
    fun obtainNewQuestion(gameId: String, index: Int): Question
}

class GoogleSheetsQuestionsProvider : QuestionProvider {

    val allQuestions: List<Question>
    private val answerOptions: List<String>

    init {
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

        val allSheetsRequest = sheets.spreadsheets()
            .values().get(QUESTIONS_FILE_ID_GOOGLE_DOC, "'questons'")
        allQuestions = allSheetsRequest.execute().getValues()
            .toRawEntries()
            .map { it.toQuestion(answerOptions) }

        println("GoogleSheetsQuestionsProvider.init(); loaded ${allQuestions.size} questions")
    }

    override fun obtainNewQuestion(gameId: String, index: Int): Question {
        return allQuestions[index]
    }
}

private fun Map<String, Any>.toQuestion(answerOptions: List<String>) = Question(
    text = stringFor("question"),
    options = answerOptions
)

private fun List<List<Any>>.toRawEntries(): List<Map<String, Any>> {
    val headers = first().map { it as String }
    val rows = drop(1)
    return rows.map { row -> headers.zip(row).toMap() }
}

private fun Map<String, Any>.stringFor(key: String) = (this[key] as? String) ?: ""