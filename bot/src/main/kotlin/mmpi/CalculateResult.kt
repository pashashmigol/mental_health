package mmpi

fun calculateMmpi(answers: Array<MmpiTest.Answer?>): MmpiTest.Result {
    val score = answers.sumOf { it?.option ?: 0 }

    return MmpiTest.Result(
        "You've got $score. It seems you have an issue"
    )
}

abstract class Scale(
    val yes: List<Int>,
    val no: List<Int>,
    val costOfZero: Int,
    val costOfKeyAnswer: Float,
    val correctionFactor: Float,
    val tA: Float,
    val tB: Float
) {
    private fun countAnswers(answers: Array<MmpiTest.Answer?>): Int {
        val numberOfYes = yes.filter { answers[it] == MmpiTest.Answer.Agree }.size
        val numberOfNo = no.filter { answers[it] == MmpiTest.Answer.Disagree }.size

        return numberOfYes + numberOfNo
    }

    fun calculate(answers: Array<MmpiTest.Answer?>): Int {
        val correction = if (correctionFactor > 0.001) {//let's say it's a zero
            CorrectionScaleK.calculate(answers)
        } else {
            0
        }
        val rawScore = (costOfZero + countAnswers(answers)
                * costOfKeyAnswer + correction).toInt()

        return (rawScore * tA + tB).toInt()
    }
}

