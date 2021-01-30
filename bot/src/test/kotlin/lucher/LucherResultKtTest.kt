package lucher

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.coroutines.suspendCoroutine

internal class LucherResultKtTest {

    @Test
    suspend fun calculateResult() {
        val firstRound = listOf("5", "1", "3", "4", "0", "6", "2", "7")
        val secondRound = listOf("3", "1", "5", "4", "0", "7", "2", "6")

        val pairs = findPairs(firstRound, secondRound)
        val expected = listOf("+3+1", "x1x5", "=4=0", "=7=2", "-2-6")

        assertEquals(expected, pairs)

        suspendCoroutine<String> { continuation ->
            continuation.context
        }
    }
}