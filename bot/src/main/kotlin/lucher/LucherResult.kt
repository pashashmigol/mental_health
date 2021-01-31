package lucher

data class LucherResult(val paragraphs: List<String>, val anxiety: Int) {
    fun description() = paragraphs.joinToString(separator = "\n")
}

class Answer(index: Int)

class LucherAnswers(
    val firstRound: List<LucherColor>,
    val secondRound: List<LucherColor>
)

fun calculateResult(answers: LucherAnswers): LucherResult {
    return LucherResult(
        paragraphs = listOf("Ты очень странный, тебя надо лечить электричеством"),
        anxiety = 0
    )
}

fun findPairs(
    firstTouchAnswers: List<String>,
    secondTouchAnswers: List<String>
): List<String> {
    assert(firstTouchAnswers.size == 8)
    assert(secondTouchAnswers.size == 8)

    val pairsFirst = firstTouchAnswers.zipWithNext()
    val pairsSecond = secondTouchAnswers.zipWithNext()

    val pairsShared = pairsSecond
        .filter { pair1 ->
            pairsFirst.any { pair2 -> (pair1.toList().toSet() == pair2.toList().toSet()) }
        }

    val last = pairsShared.size - 1

    return pairsShared.mapIndexed { index, pair ->
        when (index) {
            0 -> "+${pair.first}+${pair.second}"
            1 -> "x${pair.first}x${pair.second}"
            last -> "-${pair.first}-${pair.second}"
            else -> "=${pair.first}=${pair.second}"
        }
    }
}