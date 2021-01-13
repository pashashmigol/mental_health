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
    fun mmpiProcessQuestions(gender: MmpiProcess.Gender): List<MmpiProcess.Question>
    fun mmpiProcessScales(gender: MmpiProcess.Gender): MmpiProcess.Scales
    fun reload()
}

object CurrentQuestionsProvider : QuestionsProvider {
    private var internalProvider: QuestionsProvider? = null

    fun initGoogleSheetsProvider(rootPath: String) {
        internalProvider = GoogleSheetsQuestionsProvider(rootPath)
    }

    override fun mmpiProcessQuestions(gender: MmpiProcess.Gender): List<MmpiProcess.Question> {
        return internalProvider?.mmpiProcessQuestions(gender)!!
    }

    override fun mmpiProcessScales(gender: MmpiProcess.Gender): MmpiProcess.Scales {
        return internalProvider?.mmpiProcessScales(gender)!!
    }

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

    private var questionForMen: List<MmpiProcess.Question> = emptyList()
    private var scalesForMen: MmpiProcess.Scales? = null

    private var questionForWomen: List<MmpiProcess.Question> = emptyList()
    private var scalesForWomen: MmpiProcess.Scales? = null

    private var answerOptions: List<String> = emptyList()

    init {
        reload()
    }

    override fun mmpiProcessQuestions(gender: MmpiProcess.Gender): List<MmpiProcess.Question> {
        return when (gender) {
            MmpiProcess.Gender.Male -> questionForMen
            MmpiProcess.Gender.Female -> questionForWomen
        }
    }

    override fun mmpiProcessScales(gender: MmpiProcess.Gender): MmpiProcess.Scales {
        return when (gender) {
            MmpiProcess.Gender.Male -> scalesForMen!!
            MmpiProcess.Gender.Female -> scalesForWomen!!
        }
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
        scalesForWomen = loadScales(sheets, MmpiProcess.Gender.Female)
        scalesForMen = loadScales(sheets, MmpiProcess.Gender.Male)
    }

    private fun reloadQuestions(sheets: Sheets) {
        val answerOptionsRequest = sheets.spreadsheets()
            .values().get(QUESTIONS_FILE_ID_GOOGLE_DOC, "'answer_options'")

        answerOptions = answerOptionsRequest.execute().getValues()
            .toRawEntries()
            .map { it["answer"] as String }

        val allSheetsRequest = sheets.spreadsheets()
            .values().get(QUESTIONS_FILE_ID_GOOGLE_DOC, "'questions'")
        questionForMen = allSheetsRequest.execute().getValues()
            .toRawEntries()
            .map { it.toQuestion(answerOptions) }
    }

    private fun loadScales(sheets: Sheets, gender: MmpiProcess.Gender): MmpiProcess.Scales {
        val scalesRequest = sheets.spreadsheets()
            .values().get(QUESTIONS_FILE_ID_GOOGLE_DOC, "'scales'")

        val scalesMap = scalesRequest.execute()
            .getValues()
            .toRawEntries()
            .filter { it.isNotEmpty() }
            .map {
                val scale = toScale(it, gender)
                return@map Pair(scale.id, scale)
            }.toMap()

        return MmpiProcess.Scales(
            correctionScale = scalesMap["CorrectionScaleK"]!!,
            liesScale = scalesMap["LiesScaleL"]!!,
            credibilityScale = scalesMap["CredibilityScaleF"]!!,
            introversionScale = scalesMap["IntroversionScale0"]!!,
            overControlScale1 = scalesMap["OverControlScale1"]!!,
            passivityScale2 = scalesMap["PassivityScale2"]!!,
            labilityScale3 = scalesMap["LabilityScale3"]!!,
            impulsivenessScale4 = scalesMap["ImpulsivenessScale4"]!!,
            masculinityScale5 = scalesMap["MasculinityScale5"]!!,
            rigidityScale6 = scalesMap["RigidityScale6"]!!,
            anxietyScale7 = scalesMap["AnxietyScale7"]!!,
            individualismScale8 = scalesMap["IndividualismScale8"]!!,
            optimismScale9 = scalesMap["OptimismScale9"]!!
        )
    }

    private fun toScale(map: Map<String, Any>, gender: MmpiProcess.Gender) = Scale(
        id = map["id"] as String,
        title = map["title"] as String,
        yes = parseList(
            when (gender) {
                MmpiProcess.Gender.Male -> map["key_answers_yes_men"]
                MmpiProcess.Gender.Female ->
                    map.getOrDefault("key_answers_yes_women", map["key_answers_yes_men"])
            } as String
        ),
        no = parseList(
            when (gender) {
                MmpiProcess.Gender.Male -> map["key_answers_no_men"]
                MmpiProcess.Gender.Female ->
                    map.getOrDefault("key_answers_no_women", map["key_answers_no_men"])
            } as String
        ),
        costOfZero = (when (gender) {
            MmpiProcess.Gender.Male -> map["cost_of_zero_men"]
            MmpiProcess.Gender.Female -> map["cost_of_zero_women"]
        }.toString().toFloat()),
        costOfKeyAnswer = (when (gender) {
            MmpiProcess.Gender.Male -> map["cost_of_key_answer_men"]
            MmpiProcess.Gender.Female -> map["cost_of_key_answer_women"]
        } as String).toFloat(),
        correctionFactor = (map["correction_factor"] as String).toFloat(),
        tA = (when (gender) {
            MmpiProcess.Gender.Male -> map["t_a_men"]
            MmpiProcess.Gender.Female -> map["t_a_women"]
        } as String).toFloat(),
        tB = (when (gender) {
            MmpiProcess.Gender.Male -> map["t_b_men"]
            MmpiProcess.Gender.Female -> map["t_b_women"]
        } as String).toFloat(),
        segments = createSegments(map)
    )
}

private fun createSegments(map: Map<String, Any>): List<Segment> {
    val res = mutableListOf<Segment>()

    createSegment(map["range_1"] as String?, map["range_1_description"] as String?)?.let { res.add(it) }
    createSegment(map["range_2"] as String?, map["range_2_description"] as String?)?.let { res.add(it) }
    createSegment(map["range_3"] as String?, map["range_3_description"] as String?)?.let { res.add(it) }
    createSegment(map["range_4"] as String?, map["range_4_description"] as String?)?.let { res.add(it) }
    createSegment(map["range_5"] as String?, map["range_5_description"] as String?)?.let { res.add(it) }

    return ArrayList(res)
}

private fun createSegment(range: String?, description: String?): Segment? {
    if (range == null || description == null) {
        return null
    }
    val borders = range.split("-")
    val min = borders[0].trim().toInt()
    val max = borders[1].trim().toInt()

    return Segment(IntRange(min, max), description)
}

private fun parseList(raw: String?): List<Int> = if (raw.isNullOrBlank())
    emptyList()
else
    raw.split(",").map { it.trim().toInt() }

private fun Map<String, Any>.toQuestion(answerOptions: List<String>) = MmpiProcess.Question(
    text = stringFor("question"),
    options = answerOptions
)

private fun List<List<Any>>.toRawEntries(): List<Map<String, Any>> {
    val headers = first().map { it as String }
    val rows = drop(1)
    return rows.map { row -> headers.zip(row).toMap() }
}

private fun Map<String, Any>.stringFor(key: String) = (this[key] as? String) ?: ""