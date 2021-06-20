package lucher

import com.google.common.collect.Lists


data class LucherResult(
    val stablePairs: Map<LucherElement, String>,
    val brokenPairs: Map<LucherElement, String>,
    val contraversedPairs: Map<LucherElement, String>,
    val firstRoundAnxiety: Int,
    val secondRoundAnxiety: Int
)

data class AllPairs(
    val stablePairs: List<LucherElement>,
    val brokenPairs: List<LucherElement>,
    val contraversedPairs: List<LucherElement>
)

fun calculateLucher(answers: LucherAnswers, meanings: Map<String, String>): LucherResult {

    val firstTouchAnswers = answers.firstRound.map { it.index.toString() }
    val secondTouchAnswers = answers.secondRound.map { it.index.toString() }

    val pairs = findPairs(firstTouchAnswers, secondTouchAnswers)

    val stable = pairs.stablePairs
        .map { it to (meanings[it.toString()] ?: "") }
        .filter { it.second.isNotEmpty() }
        .toMap()

    val broken = pairs.brokenPairs
        .map { it to (meanings[it.toString()] ?: "") }
        .filter { it.second.isNotEmpty() }
        .toMap()

    val contraversed = pairs.contraversedPairs
        .map { it to (meanings[it.toString()] ?: "") }
        .filter { it.second.isNotEmpty() }
        .toMap()

    return LucherResult(
        stablePairs = stable,
        brokenPairs = broken,
        contraversedPairs = contraversed,
        firstRoundAnxiety = calculateAnxiety(firstTouchAnswers),
        secondRoundAnxiety = calculateAnxiety(secondTouchAnswers)
    )
}

fun findPairs(
    firstTouchAnswers: List<String>,
    secondTouchAnswers: List<String>
): AllPairs {
    assert(firstTouchAnswers.size == 8)
    assert(secondTouchAnswers.size == 8)

    val pairsFirst = firstTouchAnswers
        .zipWithNext()
        .map { LucherElement.Pair(it.first, it.second) }

    val pairsSecond = secondTouchAnswers
        .zipWithNext()
        .map { LucherElement.Pair(it.first, it.second) }

    val commonPairs = findCommonPairs(pairsSecond, pairsFirst)
    val isolatedColors = isolatedColors(commonPairs, secondTouchAnswers)
    val last = commonPairs.size + isolatedColors.size - 1

    val mainPairs: List<LucherElement> = secondTouchAnswers
        .mapNotNull { color: String ->
            isolatedColors.find { color == it.color.color.index.toString() }
                ?: commonPairs.find { it.firstColor.color.index.toString() == color }
        }.mapIndexed { index: Int, lucherElement: LucherElement ->
            when (index) {
                0 -> lucherElement.addAttribute("+")
                1 -> lucherElement.addAttribute("x")
                last -> lucherElement.addAttribute("-")
                else -> lucherElement.addAttribute("=")
            }
        }

    val brokenPairs = findBrokenPairs(firstTouchAnswers, secondTouchAnswers)
    val contraversedPairs = findContraversedPairs(firstTouchAnswers, secondTouchAnswers)

    return AllPairs(mainPairs, brokenPairs, contraversedPairs)
}

fun findContraversedPairs(
    firstRoundAnswers: List<String>,
    secondRoundAnswers: List<String>
): List<LucherElement.Pair> {
    val firstTouchAs: MutableList<AttributedColor> = findAnxietyColors(firstRoundAnswers)
    val firstTouchCs: MutableList<AttributedColor> = findCompensatoryColors(firstRoundAnswers)

    if (firstTouchAs.isEmpty() && firstTouchCs.isNotEmpty()) {
        firstTouchAs.add(
            AttributedColor(LucherColor.of(firstRoundAnswers[7]), "-")
        )
    }
    if (firstTouchCs.isEmpty() && firstTouchAs.isNotEmpty()) {
        firstTouchCs.add(
            AttributedColor(LucherColor.of(firstRoundAnswers[0]), "+")
        )
    }

    val secondTouchAs = findAnxietyColors(secondRoundAnswers)
    val secondTouchCs = findCompensatoryColors(secondRoundAnswers)

    if (secondTouchAs.isEmpty() && secondTouchCs.isNotEmpty()) {
        secondTouchAs.add(
            AttributedColor(LucherColor.of(secondRoundAnswers[7]), "-")
        )
    }
    if (secondTouchCs.isEmpty() && secondTouchAs.isNotEmpty()) {
        secondTouchCs.add(
            AttributedColor(LucherColor.of(secondRoundAnswers[0]), "+")
        )
    }

    return Lists.cartesianProduct(secondTouchCs, secondTouchAs)
        .map {
            LucherElement.Pair(firstColor = it[0], secondColor = it[1])
        }
}

val mainColors = setOf("1", "2", "3", "4")
val compensatoryColors = setOf("6", "7", "0")

fun findAnxietyColors(answers: List<String>): MutableList<AttributedColor> {
    val places: List<AttributedColor> = answers
        .map {
            AttributedColor(
                color = LucherColor.of(it),
                attribute = "-"
            )
        }
        .takeLast(3)

    return when (val index = places.indexOfFirst { it.color.index.toString() in mainColors }) {
        -1 -> emptyList()
        else -> places.subList(index, places.size)
    }.toMutableList()
}

fun findCompensatoryColors(answers: List<String>): MutableList<AttributedColor> {
    val places: List<AttributedColor> = answers
        .map {
            AttributedColor(
                color = LucherColor.of(it),
                attribute = "+"
            )
        }
        .take(3)

    return when (val index = places.indexOfLast { it.color.index.toString() in compensatoryColors }) {
        -1 -> emptyList()
        else -> places.subList(0, index + 1)
    }.toMutableList()
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
    commonPairs: List<LucherElement.Pair>,
    secondTouchAnswers: List<String>
): List<LucherElement.Single> {
    val pairedColors: Set<String> =
        commonPairs
            .flatMap { listOf(it.firstColor, it.secondColor) }
            .map { it.color.index.toString() }
            .toSet()

    return secondTouchAnswers
        .filterNot { pairedColors.contains(it) }
        .map { LucherElement.Single(it) }
}

internal fun findBrokenPairs(
    firstRoundAnswers: List<String>,
    secondRoundAnswers: List<String>
): List<LucherElement.Pair> {

    val pairsFirst: List<LucherElement.Pair> = firstRoundAnswers
        .chunked(2)
        .mapIndexed { index: Int, list: List<String> ->
            val attribute = when (index) {
                0 -> "+"
                1 -> "x"
                2 -> "="
                3 -> "-"
                else -> null
            }!!
            LucherElement.Pair(
                AttributedColor(LucherColor.of(list[0]), attribute),
                AttributedColor(LucherColor.of(list[1]), attribute)
            )
        }

    val pairsSecond: List<LucherElement.Pair> = secondRoundAnswers
        .zipWithNext()
        .map {
            LucherElement.Pair(
                AttributedColor(it.first),
                AttributedColor(it.second)
            )
        }

    return pairsFirst
        .filterNot { first ->
            pairsSecond.any { second ->
                first.sameColors(second)
            }
        }
}

private fun findCommonPairs(
    pairsSecond: List<LucherElement.Pair>,
    pairsFirst: List<LucherElement.Pair>
): List<LucherElement.Pair> = pairsSecond
    .filter { pair1 ->
        pairsFirst.any { pair2 ->
            pair1.sameColors(pair2)
        }
    }