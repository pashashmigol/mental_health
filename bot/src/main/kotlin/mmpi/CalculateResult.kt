package mmpi

fun calculate(answers: Array<Mmpi566.Answer?>): Mmpi566.Result {
    val score = answers.sumOf { it?.option ?: 0 }

    return Mmpi566.Result(
        description = "You've got $score. It seems you have an issue",
        liesScale = LiesScaleL.calculate(answers),
        credibilityScale = CredibilityScaleF.calculate(answers),
        introversionScale = IntroversionScale0.calculate(answers),
        overControlScale1 = OverControlScale1.calculate(answers),
        passivityScale2 = PassivityScale2.calculate(answers),
        labilityScale3 = LabilityScale3.calculate(answers),
        impulsivenessScale4 = ImpulsivenessScale4.calculate(answers),
        masculinityScale5 = MasculinityScale5M.calculate(answers),
        rigidityScale6 = RigidityScale6.calculate(answers),
        anxietyScale7 = AnxietyScale7.calculate(answers),
        individualismScale8 = IndividualismScale8.calculate(answers),
        optimismScale9 = OptimismScale9.calculate(answers)
    )
}

abstract class Scale(
    val name: String,
    val yes: List<Int>,
    val no: List<Int>,
    val costOfZero: Int,
    val costOfKeyAnswer: Float,
    val correctionFactor: Float,
    val tA: Float,
    val tB: Float
) {
    private fun countAnswers(answers: Array<Mmpi566.Answer?>): Int {
        val numberOfYes = yes.filter { answers[it] == Mmpi566.Answer.Agree }.size
        val numberOfNo = no.filter { answers[it] == Mmpi566.Answer.Disagree }.size

        return numberOfYes + numberOfNo
    }

    fun calculate(answers: Array<Mmpi566.Answer?>): Result {
        val correction = if (correctionFactor > 0.001) {//let's say it's a zero
            CorrectionScaleK.calculate(answers).score
        } else {
            0
        }
        val rawScore = (costOfZero + countAnswers(answers)
                * costOfKeyAnswer + correction).toInt()

        val finalScore = (rawScore * tA + tB).toInt()

        return Result(
            name = name,
            score = finalScore,
            description = "Visit a doctor!!!"
        )
    }

    data class Result(val name: String, val score: Int, val description: String)
}

