package lucher

data class LucherResult(val paragraphs: List<String>, val anxiety: Int) {
    fun description() = paragraphs.joinToString(separator = "\n")
}

class Answer(index: Int)

class LucherAnswers(
    val firstTouch: List<LucherColor>,
    val secondTouch: List<LucherColor>
)

fun calculateResult(answers: LucherAnswers): LucherResult {
    return LucherResult(
        paragraphs = listOf("Ты очень странный, тебя надо лечить электричеством"),
        anxiety = 0
    )
}

internal fun findPairs(
    firstTouchAnswers: List<String>,
    secondTouchAnswers: List<String>
): List<String> {
    return emptyList()
}