package mmpi

import kotlin.math.roundToInt

fun calculate(answers: List<MmpiProcess.Answer?>, scales: MmpiProcess.Scales): MmpiProcess.Result {

    val correction = scales.correctionScale.calculate(answers).raw

    return MmpiProcess.Result(
        liesScaleL = scales.liesScale.calculate(answers, correction),
        credibilityScaleF = scales.credibilityScale.calculate(answers, correction),
        correctionScaleK = scales.correctionScale.calculate(answers, correction),
        introversionScale0 = scales.introversionScale.calculate(answers, correction),
        overControlScale1 = scales.overControlScale1.calculate(answers, correction),
        passivityScale2 = scales.passivityScale2.calculate(answers, correction),
        labilityScale3 = scales.labilityScale3.calculate(answers, correction),
        impulsivenessScale4 = scales.impulsivenessScale4.calculate(answers, correction),
        masculinityScale5 = scales.masculinityScale5.calculate(answers, correction),
        rigidityScale6 = scales.rigidityScale6.calculate(answers, correction),
        anxietyScale7 = scales.anxietyScale7.calculate(answers, correction),
        individualismScale8 = scales.individualismScale8.calculate(answers, correction),
        optimismScale9 = scales.optimismScale9.calculate(answers, correction)
    )
}

class Scale(
    val id: String,
    val title: String,
    val yes: List<Int>,
    val no: List<Int>,
    val correctionFactor: Float,
    val tA: Float,
    val tB: Float,
    private val segments: List<Segment>
) {
    fun calculate(answers: List<MmpiProcess.Answer?>, correctionValue: Int = 0): Result {

        val rawScore = rawScore(answers)
        val rawScoreCorrected = rawScoreCorrected(rawScore, correctionValue)
        val finalScore = (rawScoreCorrected * tA + tB).toInt()

        val description = segments.firstOrNull {
            it.range.contains(finalScore)
        }?.description ?: ""

        return Result(
            name = title,
            score = finalScore,
            description = description,
            raw = rawScoreCorrected
        )
    }

    private fun rawScore(answers: List<MmpiProcess.Answer?>): Int {
        val numberOfYes = yes.filter { answers[it - 1] == MmpiProcess.Answer.Agree }.size
        val numberOfNo = no.filter { answers[it - 1] == MmpiProcess.Answer.Disagree }.size

        return numberOfYes + numberOfNo
    }

    private fun rawScoreCorrected(rawScore: Int, correction: Int): Int {
        return (rawScore + correction * correctionFactor).roundToInt()
    }

    override fun toString(): String {
        return "$id(yes=$yes, no=$no)"
    }

    data class Result(val name: String, val score: Int, val description: String, val raw: Int)
}

data class Segment(val range: IntRange, val description: String)

