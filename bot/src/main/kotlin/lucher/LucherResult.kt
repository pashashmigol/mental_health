package lucher

import com.google.common.collect.Lists

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

    val commonPairs = findCommonPairs(pairsSecond, pairsFirst)
    val isolatedColors = isolatedColors(commonPairs, secondTouchAnswers)

    val last = commonPairs.size + isolatedColors.size - 1

    return secondTouchAnswers
        .mapNotNull { color: String ->
            isolatedColors.find { it.color == color } ?: commonPairs.find { it.color1 == color }
        }
        .mapIndexed { index: Int, element: Element ->
            when (index) {
                0 -> element.toString("+")
                1 -> element.toString("x")
                last -> element.toString("-")
                else -> element.toString("=")
            }
        }
}

fun findContraversedPairs(
    firstTouchAnswers: List<String>,
    secondTouchAnswers: List<String>
): Set<String> {
    val firstTouchAs = findAnxietyColors(firstTouchAnswers)
    val firstTouchCs = findCompensatoryColors(firstTouchAnswers)

    val secondTouchAs = findAnxietyColors(secondTouchAnswers)
    val secondTouchCs = findCompensatoryColors(secondTouchAnswers)

    val pairsFirst = Lists.cartesianProduct(firstTouchCs, firstTouchAs)
        .map { "+${it[0]}-${it[1]}" }.toMutableSet()

    val pairsSecond = Lists.cartesianProduct(secondTouchCs, secondTouchAs)
        .map { "+${it[0]}-${it[1]}" }.toSet()
    pairsFirst.addAll(pairsSecond)

    return pairsFirst
}

val mainColors = setOf("1", "2", "3", "4")
val compensatoryColors = setOf("6", "7", "0")

fun findAnxietyColors(answers: List<String>): List<String> {
    val places = answers.takeLast(3)

    return when (val index = places.indexOfFirst { it in mainColors }) {
        -1 -> return emptyList()
        else -> places.subList(index, places.size)
    }
}

fun findCompensatoryColors(answers: List<String>): List<String> {
    val places = answers.take(3)

    return when (val index = places.indexOfLast { it in compensatoryColors }) {
        -1 -> return emptyList()
        else -> places.subList(0, index + 1)
    }
}

fun calculateAnxiety(answers: List<String>): Int {
    val anxiety1 = answers.foldIndexed(0) { i: Int, acc: Int, element: String ->
        return@foldIndexed acc + if (element in mainColors)
            when (i) {
                5 -> 1
                6 -> 2
                7 -> 3
                else -> 0
            }
        else 0
    }
    val anxiety2 = answers.foldIndexed(0) { i: Int, acc: Int, element: String ->
        return@foldIndexed acc + if (element in compensatoryColors)
            when (i) {
                0 -> 3
                1 -> 2
                2 -> 1
                else -> 0
            }
        else 0
    }

    return anxiety1 + anxiety2
}


private fun isolatedColors(
    commonPairs: List<Element.Pair>,
    secondTouchAnswers: List<String>
): List<Element.Single> {
    val pairedColors = commonPairs.flatMap { listOf(it.color1, it.color2) }.toSet()

    return secondTouchAnswers
        .filterNot { pairedColors.contains(it) }
        .map { Element.Single(it) }
}

private fun findCommonPairs(
    pairsSecond: List<Pair<String, String>>,
    pairsFirst: List<Pair<String, String>>
) = pairsSecond
    .filter { pair1 ->
        pairsFirst.any { pair2 -> (pair1.toList().toSet() == pair2.toList().toSet()) }
    }
    .map {
        Element.Pair(it.first, it.second)
    }

private sealed class Element {
    abstract fun toString(prefix: String): String

    data class Single(val color: String) : Element() {
        override fun toString(prefix: String) = "$prefix$color"
    }

    data class Pair(val color1: String, val color2: String) : Element() {
        override fun toString(prefix: String) = "$prefix$color1$prefix$color2"
    }
}