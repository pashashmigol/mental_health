package mmpi

fun agreeTo(vararg i: Int): List<MmpiProcess.Answer> {
    val answers = i.toSet()
    return (0..565).map {
        if (it in answers)
            MmpiProcess.Answer.Agree
        else
            MmpiProcess.Answer.Disagree
    }
}

val allAgree566 = (0..565).map {
    MmpiProcess.Answer.Agree
}

val allAgree377 = (0..376).map {
    MmpiProcess.Answer.Agree
}

val oneAfterOne566 = (0..565).map {
    if ((it % 2) == 0)
        MmpiProcess.Answer.Agree
    else
        MmpiProcess.Answer.Disagree
}
val oneAfterOne377 = (0..376).map {
    if ((it % 2) == 1)
        MmpiProcess.Answer.Agree
    else
        MmpiProcess.Answer.Disagree
}

val justFewAnswers = listOf(
    MmpiProcess.Answer.Agree,
    MmpiProcess.Answer.Disagree
)