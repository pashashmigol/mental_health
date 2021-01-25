package lucher

data class LucherResult(val paragraphs: List<String>, val anxiety: Int)

data class LucherAnswers

private fun calculateResult(
    firstTouchAnswers: List<String>,
    secondTouchAnswers: List<String>
): LucherResult {


    return LucherResult(
        paragraphs = listOf("Ты очень странный, тебя надо лечить электричеством"),
        anxiety = 1
    )
}