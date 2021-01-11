package mmpi

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
    val mmpi566Questions: List<Mmpi566.Question>
    val mmpi566Scales: Mmpi566.Scales?
    fun reload()
}

object CurrentQuestionsProvider : QuestionsProvider {
    private var internalProvider: QuestionsProvider? = null

    fun initGoogleSheetsProvider(rootPath: String) {
        internalProvider = GoogleSheetsQuestionsProvider(rootPath)
    }

    override val mmpi566Questions
        get() = internalProvider?.mmpi566Questions ?: emptyList()
    override val mmpi566Scales
        get() = internalProvider?.mmpi566Scales

    override fun reload() {
        internalProvider?.reload()
    }
}

class GoogleSheetsQuestionsProvider(projectRoot: String) : QuestionsProvider {
    companion object {
        private const val CREDENTIALS_FILE_NAME = "mental-health-300314-1be17f2cdb6f.json"
    }

    private val serviceAccount = FileInputStream("$projectRoot$CREDENTIALS_FILE_NAME")
    private val credentials: GoogleCredentials = GoogleCredentials.fromStream(serviceAccount)

    private var _allQuestions: List<Mmpi566.Question> = emptyList()
    private var _scales: Mmpi566.Scales? = null

    private var answerOptions: List<String> = emptyList()

    override val mmpi566Questions: List<Mmpi566.Question>
        get() = _allQuestions
    override val mmpi566Scales: Mmpi566.Scales
        get() = _scales!!

    init {
        reload()
    }

    override fun reload() {
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val jacksonFactory = JacksonFactory.getDefaultInstance()
        val scopes = listOf(SheetsScopes.SPREADSHEETS_READONLY)
        val local: HttpRequestInitializer by lazy {
            HttpCredentialsAdapter(credentials.createScoped(scopes))
        }

        val sheets = Sheets.Builder(transport, jacksonFactory, local).build()

        reloadQuestions(sheets)
        reloadScales(sheets)
    }

    private fun reloadQuestions(sheets: Sheets) {
        val answerOptionsRequest = sheets.spreadsheets()
            .values().get(QUESTIONS_FILE_ID_GOOGLE_DOC, "'answer_options'")

        answerOptions = answerOptionsRequest.execute().getValues()
            .toRawEntries()
            .map { it["answer"] as String }

        val allSheetsRequest = sheets.spreadsheets()
            .values().get(QUESTIONS_FILE_ID_GOOGLE_DOC, "'questions'")
        _allQuestions = allSheetsRequest.execute().getValues()
            .toRawEntries()
            .map { it.toQuestion(answerOptions) }


    }

    private fun reloadScales(sheets: Sheets) {
        val scalesRequest = sheets.spreadsheets()
            .values().get(QUESTIONS_FILE_ID_GOOGLE_DOC, "'scales'")
        val scales = scalesRequest.execute()
            .getValues()
            .toRawEntries()
            .filter { it.isNotEmpty() }
            .map {
                val scale = Scale(
                    id = it["id"] as String,
                    title = it["title"] as String,
                    yes = parseList(it["key_answers_yes_men"] as String),
                    no = parseList(it["key_answers_no_men"] as String),
                    costOfZero = (it["cost_of_zero_men"] as String).toInt(),
                    costOfKeyAnswer = (it["cost_of_key_answer_men"] as String).toFloat(),
                    correctionFactor = (it["correction_factor"] as String).toFloat(),
                    tA = (it["t_a_men"] as String).toFloat(),
                    tB = (it["t_b_men"] as String).toFloat()
                )
                return@map Pair(scale.id, scale)
            }.toMap()

        _scales = Mmpi566.Scales(
            correctionScale = scales["CorrectionScaleK"]!!,
            liesScale = scales["LiesScaleL"]!!,
            credibilityScale = scales["CredibilityScaleF"]!!,
            introversionScale = scales["IntroversionScale0"]!!,
            overControlScale1 = scales["OverControlScale1"]!!,
            passivityScale2 = scales["PassivityScale2"]!!,
            labilityScale3 = scales["LabilityScale3"]!!,
            impulsivenessScale4 = scales["ImpulsivenessScale4"]!!,
            masculinityScale5 = scales["MasculinityScale5"]!!,
            rigidityScale6 = scales["RigidityScale6"]!!,
            anxietyScale7 = scales["AnxietyScale7"]!!,
            individualismScale8 = scales["IndividualismScale8"]!!,
            optimismScale9 = scales["OptimismScale9"]!!
        )
    }
}

private fun parseList(raw: String?): List<Int> {
    return if (raw.isNullOrBlank())
        emptyList()
    else
        raw.split(",").map { it.trim().toInt() }
}

private fun Map<String, Any>.toQuestion(answerOptions: List<String>) = Mmpi566.Question(
    text = stringFor("question"),
    options = answerOptions
)

private fun List<List<Any>>.toRawEntries(): List<Map<String, Any>> {
    val headers = first().map { it as String }
    val rows = drop(1)
    return rows.map { row -> headers.zip(row).toMap() }
}

private fun Map<String, Any>.stringFor(key: String) = (this[key] as? String) ?: ""