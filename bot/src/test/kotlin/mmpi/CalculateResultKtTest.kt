package mmpi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class CalculateResultKtTest {

    @Test
    fun calculateMmpi() {
        assertFalse(true)
    }

    @Test
    fun checkScales() {
        assertEquals(15, scaleSum(scaleL))
        assertEquals(64, scaleSum(scaleF))
        assertEquals(30, scaleSum(scaleK))
        assertEquals(33, scaleSum(scale1))
        assertEquals(60, scaleSum(scale2))
        assertEquals(60, scaleSum(scale3))
        assertEquals(50, scaleSum(scale4))
        assertEquals(60, scaleSum(scale5M))
        assertEquals(60, scaleSum(scale5F))
        assertEquals(40, scaleSum(scale6))
        assertEquals(48, scaleSum(scale7))
        assertEquals(78, scaleSum(scale8))
        assertEquals(46, scaleSum(scale9))
        assertEquals(70, scaleSum(scale0))
    }
}

private fun scaleSum(scale: Scale) = scale.yes.size + scale.no.size
