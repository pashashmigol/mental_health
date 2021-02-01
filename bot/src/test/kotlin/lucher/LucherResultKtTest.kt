package lucher

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.coroutines.suspendCoroutine

internal class LucherResultKtTest {

    @Test
    fun calculateResult() {
        val firstRound = listOf("5", "1", "3", "4", "0", "6", "2", "7")
        val secondRound = listOf("3", "1", "5", "4", "0", "7", "2", "6")

        val pairs = findPairs(firstRound, secondRound)
        val expected = listOf("+3+1", "x1x5", "=4=0", "=7=2", "-2-6")

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
        val expected = listOf("+3+1", "x4", "=6=0", "=2=7", "-5")

        assertEquals(expected, pairs)
    }
}