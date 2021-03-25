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

fun disagreeTo(vararg i: Int): List<MmpiProcess.Answer> {
    val answers = i.toSet()
    return (0..565).map {
        if (it in answers)
            MmpiProcess.Answer.Disagree
        else
            MmpiProcess.Answer.Agree
    }
}

fun answers(
    size: Int,
    agree: Set<Int>,
    disagree: Set<Int>
): List<MmpiProcess.Answer> = (0 until size).map {
    when (it + 1) {
        in agree -> MmpiProcess.Answer.Agree
        in disagree -> MmpiProcess.Answer.Disagree
        else -> MmpiProcess.Answer.Disagree
    }
}

val allAgree566 = (0..565).map {
    MmpiProcess.Answer.Agree
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