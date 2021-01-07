package mmpi

fun calculateMmpi(answers: Array<MmpiTest.Answer?>): MmpiTest.Result{
    val score = answers.sumOf { it?.option ?: 0 }


    return MmpiTest.Result(
        "You've got $score. It seems you have an issue")
}