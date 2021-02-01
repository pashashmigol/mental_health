package lucher

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LucherResultKtTest {

    @Test
    fun calculateResult() {
        val firstRound = listOf("5", "1", "3", "4", "0", "6", "2", "7")
        val secondRound = listOf("3", "1", "5", "4", "0", "7", "2", "6")

        val pairs = findPairs(firstRound, secondRound)
        val expected = listOf("+3+1", "x1x5", "=4=0", "=7=2", "-2-6", "+3-6", "+3-2")

        assertEquals(expected, pairs)
    }

    @Test
    fun isolated_color() {
        val firstRound = listOf("3", "1", "5", "4", "2", "6", "0", "7")
        val secondRound = listOf("3", "5", "1", "4", "2", "6", "7", "0")

        val pairs = findPairs(firstRound, secondRound)
        val expected = listOf("+3", "x5x1", "=4=2", "=2=6", "-7-0")

        assertEquals(expected, pairs)
    }

    @Test
    fun broken_pairs() {
        val firstRound = listOf("3", "1", "5", "4", "0", "6", "7", "2")
        val secondRound = listOf("3", "1", "4", "6", "0", "2", "7", "5")

        val pairs = findPairs(firstRound, secondRound)
        val expected = listOf("+3+1", "x4", "x4x5", "=6=0", "=2=7", "-5")

        assertEquals(expected.toSet(), pairs.toSet())
    }

    @Test
    fun compensatory_pairs() {
        val firstRound = listOf("0", "6", "5", "1", "3", "4", "2", "7")
        val secondRound = listOf("7", "0", "6", "1", "5", "2", "4", "3")

        val pairs = findContraversedPairs(firstRound, secondRound)
        val expected = listOf(
            "+7-3", "+7-4", "+0-3", "+0-4", "+0-7",
            "+7-2", "+0-2", "+6-3",
            "+6-4", "+6-2"
        )

        assertEquals(expected.sorted(), pairs.sorted())
    }

    @Test
    fun findAnxietyColors(){
        val actual = findAnxietyColors(listOf("0", "6", "5", "1", "3", "4", "2", "7"))
        assertEquals(listOf("4", "2", "7"), actual)
    }

    @Test
    fun findCompensatoryColors(){
        val actual = findCompensatoryColors(listOf("0", "6", "5", "1", "3", "4", "2", "7"))
        assertEquals(listOf("0", "6"), actual)
    }

    @Test
    fun anxiety() {
        val firstRound = listOf("0", "6", "5", "1", "3", "4", "2", "7")
        val secondRound = listOf("7", "0", "6", "1", "5", "2", "4", "3")

        assertEquals(8, calculateAnxiety(firstRound))
        assertEquals(12, calculateAnxiety(secondRound))
    }
}