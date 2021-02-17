package mmpi

import Gender
import models.Question
import Settings.QUESTIONS_FILE_ID_GOOGLE_DOC
import storage.GoogleDriveConnection

fun loadMmpiData(connection: GoogleDriveConnection): MmpiData {

    val questionsMen = reloadQuestions(connection, Gender.Male)
    val questionWomen = reloadQuestions(connection, Gender.Female)

    val scalesMen = loadScales(connection, Gender.Male)
    val scalesWomen = loadScales(connection, Gender.Female)

    return MmpiData(
        questionsForMen = questionsMen,
        questionsForWomen = questionWomen,
        scalesForMen = scalesMen,
        scalesForWomen = scalesWomen
    )
}

private fun reloadQuestions(
    connection: GoogleDriveConnection,
    gender: Gender
): List<Question> {

    val answersPage = when (gender) {
        Gender.Male -> "'answer_options_men'"
        Gender.Female -> "'answer_options_women'"
    }

    val answerOptions = connection.loadDataFromFile(
        fileId = QUESTIONS_FILE_ID_GOOGLE_DOC,
        page = answersPage
    ).map { it["answer"].toString() }

    val questionsPage = when (gender) {
        Gender.Male -> "'questions_men'"
        Gender.Female -> "'questions_women'"
    }

    val questions = connection.loadDataFromFile(
        fileId = QUESTIONS_FILE_ID_GOOGLE_DOC,
        page = questionsPage
    ).map { it.toQuestion(answerOptions) }

    val size = questions.size
    return questions.mapIndexed { i: Int, question: Question ->
        question.copy(text = "(${i + 1}/$size) ${question.text}:")
    }
}

private fun loadScales(
    connection: GoogleDriveConnection,
    gender: Gender
): MmpiTestingProcess.Scales {

    val scalesMap = connection.loadDataFromFile(
        fileId = QUESTIONS_FILE_ID_GOOGLE_DOC,
        page = "'scales'"
    )
        .filter { it.isNotEmpty() }
        .map {
            val scale = toScale(it, gender)
            return@map Pair(scale.id, scale)
        }.toMap()

    return MmpiTestingProcess.Scales(
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

private fun toScale(map: Map<String, Any>, gender: Gender) = Scale(
    id = map["id"] as String,
    title = map["title"] as String,
    yes = parseList(
        when (gender) {
            Gender.Male -> map["key_answers_yes_men"]
            Gender.Female ->
                map.getOrDefault("key_answers_yes_women", map["key_answers_yes_men"])
        } as String
    ),
    no = parseList(
        when (gender) {
            Gender.Male -> map["key_answers_no_men"]
            Gender.Female ->
                map.getOrDefault("key_answers_no_women", map["key_answers_no_men"])
        } as String
    ),
    costOfZero = (when (gender) {
        Gender.Male -> map["cost_of_zero_men"]
        Gender.Female -> map["cost_of_zero_women"]
    }.toString().toFloat()),
    costOfKeyAnswer = (when (gender) {
        Gender.Male -> map["cost_of_key_answer_men"]
        Gender.Female -> map["cost_of_key_answer_women"]
    } as String).toFloat(),
    correctionFactor = (map["correction_factor"] as String).toFloat(),
    tA = (when (gender) {
        Gender.Male -> map["t_a_men"]
        Gender.Female -> map["t_a_women"]
    } as String).toFloat(),
    tB = (when (gender) {
        Gender.Male -> map["t_b_men"]
        Gender.Female -> map["t_b_women"]
    } as String).toFloat(),
    segments = createSegments(map)
)

private fun parseList(raw: String?): List<Int> = if (raw.isNullOrBlank())
    emptyList()
else
    raw.split(",").map { it.trim().toInt() }

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

private fun Map<String, Any>.toQuestion(answerOptions: List<String>) = Question(
    text = stringFor("question"),
    options = answerOptions
)

private fun Map<String, Any>.stringFor(key: String) = (this[key] as? String) ?: ""

