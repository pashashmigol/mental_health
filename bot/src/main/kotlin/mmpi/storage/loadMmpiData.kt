package mmpi.storage

import Gender
import models.Question
import mmpi.MmpiData
import mmpi.MmpiProcess
import mmpi.Scale
import mmpi.Segment
import storage.CentralDataStorage.string
import storage.GoogleDriveConnection

fun loadMmpiData(connection: GoogleDriveConnection, fileId: String): MmpiData {

    val questionsMen = reloadQuestions(connection, Gender.Male, fileId)
    val questionWomen = reloadQuestions(connection, Gender.Female, fileId) ?: questionsMen

    val scalesMen = loadScales(connection, Gender.Male, fileId)
    val scalesWomen = loadScales(connection, Gender.Female, fileId)

    return MmpiData(
        questionsForMen = questionsMen!!,
        questionsForWomen = questionWomen!!,
        scalesForMen = scalesMen,
        scalesForWomen = scalesWomen
    )
}

private fun reloadQuestions(
    connection: GoogleDriveConnection,
    gender: Gender,
    fileId: String
): List<Question>? {

    val answersPage = when (gender) {
        Gender.Male -> "'answer_options_men'"
        Gender.Female -> "'answer_options_women'"
    }

    val answerOptions: List<String>? =
        (connection.loadDataFromFile(
            fileId = fileId,
            page = answersPage
        ) ?: connection.loadDataFromFile(
            fileId = fileId,
            page = "'answer_options_men'"
        ))?.map { it["answer"].toString() }

    answerOptions!!

    val questionsPage = when (gender) {
        Gender.Male -> "'questions_men'"
        Gender.Female -> "'questions_women'"
    }

    val questions = connection.loadDataFromFile(
        fileId = fileId,
        page = questionsPage
    )?.mapIndexed { index, map ->
        map.toQuestion(index, answerOptions)
    }

    val size = questions?.size
    return questions?.mapIndexed { i: Int, question: Question ->
        question.copy(text = "(${i + 1} ${string("of")} $size) ${question.text}:")
    }
}

private fun loadScales(
    connection: GoogleDriveConnection,
    gender: Gender,
    fileId: String
): MmpiProcess.Scales {

    val scalesMap = connection.loadDataFromFile(
        fileId = fileId,
        page = "'scales'"
    )!!
        .filter { it.isNotEmpty() }.associate {
            val scale = toScale(it, gender)
            Pair(scale.id, scale)
        }

    return MmpiProcess.Scales(
        correctionScaleK = scalesMap["CorrectionScaleK"]!!,
        liesScaleL = scalesMap["LiesScaleL"]!!,
        credibilityScaleF = scalesMap["CredibilityScaleF"]!!,
        introversionScale0 = scalesMap["IntroversionScale0"]!!,
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
            Gender.Female -> map["key_answers_yes_women"]
                .takeIf { (it as String).isNotBlank() } ?: map["key_answers_yes_men"]
        } as String
    ),
    no = parseList(
        when (gender) {
            Gender.Male -> map["key_answers_no_men"]
            Gender.Female -> map["key_answers_no_women"]
                .takeIf { (it as String).isNotBlank() } ?: map["key_answers_no_men"]
        } as String
    ),
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

private fun Map<String, Any>.toQuestion(index: Int, answerOptions: List<String>) = Question(
    index = index,
    text = stringFor("question"),
    options = answerOptions.mapIndexed { i, answer ->
        val tag = when (i) {
            0 -> MmpiProcess.Answer.Agree.name
            1 -> MmpiProcess.Answer.Disagree.name
            else -> MmpiProcess.Answer.Agree.name
        }
        Question.Option(answer, tag)
    }
)

private fun Map<String, Any>.stringFor(key: String) = (this[key] as? String) ?: ""

