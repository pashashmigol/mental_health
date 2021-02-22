package mmpi

fun calculate(answers: List<MmpiProcess.Answer?>, scales: MmpiProcess.Scales): MmpiProcess.Result {
    val score = answers.sumOf { it?.option ?: 0 }

    val correction = scales.correctionScale.calculate(answers).score

    return MmpiProcess.Result(
        description = "You've got $score. It seems you have an issue",
        liesScale = scales.liesScale.calculate(answers, correction),
        credibilityScale = scales.credibilityScale.calculate(answers, correction),
        correctionScale = scales.correctionScale.calculate(answers, correction),
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
    val costOfZero: Float,
    val costOfKeyAnswer: Float,
    val correctionFactor: Float,
    val tA: Float,
    val tB: Float,
    private val segments: List<Segment>
) {
    private fun countAnswers(answers: List<MmpiProcess.Answer?>): Int {
        val numberOfYes = yes.filter { answers[it] == MmpiProcess.Answer.Agree }.size
        val numberOfNo = no.filter { answers[it] == MmpiProcess.Answer.Disagree }.size

        return numberOfYes + numberOfNo
    }

    fun calculate(answers: List<MmpiProcess.Answer?>, correctionValue: Int = 0): Result {
        val correction = correctionValue * correctionFactor

        val rawScore = (costOfZero + countAnswers(answers)
                * costOfKeyAnswer + correction).toInt()

        val finalScore = (rawScore * tA + tB).toInt()

        val description = segments.firstOrNull {
            it.range.contains(finalScore)
        }?.description ?: ""

        return Result(
            name = title,
            score = finalScore,
            description = description
        )
    }

    data class Result(val name: String, val score: Int, val description: String)
}

data class Segment(val range: IntRange, val description: String)

