package lucher

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class LucherCalculationsTest {

    @Test
    fun `calculate result`() {
        val firstRound = listOf("5", "1", "3", "4", "0", "6", "2", "7")
        val secondRound = listOf("3", "1", "5", "4", "0", "7", "2", "6")

        val expectedStablePairs = listOf("+3+1", "x1x5", "=4=0", "=7=2", "-2-6").sorted()
        val expectedContraPairs = listOf("+3-6", "+3-2").sorted()

        val actualStablePairs = findPairs(firstRound, secondRound)
            .stablePairs.map { it.toString() }.sorted()
        assertEquals(expectedStablePairs, actualStablePairs)

        val actualContraPairs = findPairs(firstRound, secondRound)
            .contraversedPairs.map { it.toString() }.sorted()
        assertEquals(expectedContraPairs, actualContraPairs)
    }

    @Test
    fun `calculate result 1`() {
        val firstRound = listOf("5", "1", "3", "4", "0", "6", "2", "7")
        val secondRound = listOf("3", "1", "5", "4", "0", "7", "2", "6")

        val expected = listOf("+3-6", "+3-2").sorted()

        val actual = findContraversedPairs(firstRound, secondRound)
            .map { it.toString() }.sorted()

        assertEquals(expected, actual)
    }

    @Test
    fun `isolated color`() {
        val firstRound = listOf("3", "1", "5", "4", "2", "6", "0", "7")
        val secondRound = listOf("3", "5", "1", "4", "2", "6", "7", "0")

        val expected = listOf("+3", "x5x1", "=4=2", "=2=6", "-7-0").sorted()

        val actual = findPairs(firstRound, secondRound).stablePairs
            .map { it.toString() }
            .sorted()

        assertEquals(expected, actual)
    }

    @Test
    fun `common case with broken pairs`() {
        val firstRound = listOf("3", "1", "5", "4", "0", "6", "7", "2")
        val secondRound = listOf("3", "1", "4", "6", "0", "2", "7", "5")

        val expectedStable = listOf("+3+1", "x4", "=6=0", "=2=7", "-5").sorted()
        val actualStable = findPairs(firstRound, secondRound)
            .stablePairs.map { it.toString() }.sorted()

        assertEquals(expectedStable, actualStable)

        val expectedBroken = listOf("x5x4")
        val actualBroken = findPairs(firstRound, secondRound)
            .brokenPairs.map { it.toString() }.sorted()

        assertEquals(expectedBroken, actualBroken)

        val expectedContra = listOf("+3-2", "+3-7", "+3-5").sorted()
        val actualContra = findPairs(firstRound, secondRound)
            .contraversedPairs.map { it.toString() }.sorted()

        assertEquals(expectedContra, actualContra)
    }


    @Test
    fun `find broken pairs`() {
        val firstRound = listOf("3", "1", "5", "4", "0", "6", "7", "2")
        val secondRound = listOf("3", "1", "4", "6", "0", "2", "7", "5")

        val actual = findBrokenPairs(firstRound, secondRound)

        val expected = listOf(
            LucherElement.Pair(
                AttributedColor("5", "x"),
                AttributedColor("4", "x"),
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `find broken pairs on equal sets`() {
        val firstRound = listOf(
            LucherColor.Blue,
            LucherColor.Yellow,
            LucherColor.Red,
            LucherColor.Green,
            LucherColor.Brown,
            LucherColor.Violet,
            LucherColor.Gray,
            LucherColor.Black
        ).map { it.index.toString() }

        val secondRound = listOf(
            LucherColor.Blue,
            LucherColor.Yellow,
            LucherColor.Red,
            LucherColor.Green,
            LucherColor.Brown,
            LucherColor.Violet,
            LucherColor.Gray,
            LucherColor.Black
        ).map { it.index.toString() }

        val actual = findBrokenPairs(firstRound, secondRound)

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `compensatory pairs`() {
        val firstRound = listOf("0", "6", "5", "1", "3", "4", "2", "7")
        val secondRound = listOf("7", "0", "6", "1", "5", "2", "4", "3")

        val expected = listOf(
            "+7-3", "+7-4", "+0-3", "+0-4",
            "+7-2", "+0-2", "+6-3",
            "+6-4", "+6-2"
        ).sorted()

        val actual = findContraversedPairs(firstRound, secondRound)
            .map { it.toString() }
            .sorted()

        assertEquals(expected, actual)
    }

    @Test
    fun `find anxiety colors`() {
        val actual = findAnxietyColors(listOf("0", "6", "5", "1", "3", "4", "2", "7"))
            .map { it.toString() }
        assertEquals(listOf("-4", "-2", "-7"), actual)
    }

    @Test
    fun `find compensatory colors`() {
        val actual = findCompensatoryColors(listOf("0", "6", "5", "1", "3", "4", "2", "7"))
            .map { it.toString() }
        assertEquals(listOf("+0", "+6"), actual)
    }

    @Test
    fun `calculate anxiety`() {
        val firstRound = listOf("0", "6", "5", "1", "3", "4", "2", "7")
        val secondRound = listOf("7", "0", "6", "1", "5", "2", "4", "3")

        assertEquals(8, calculateAnxiety(firstRound))
        assertEquals(12, calculateAnxiety(secondRound))
    }
}