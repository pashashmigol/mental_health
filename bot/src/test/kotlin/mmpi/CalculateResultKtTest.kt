package mmpi

import Gender
import models.Type
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import storage.CentralDataStorage

internal class CalculateResultKtTest {

    @BeforeEach
    fun setup() {
        CentralDataStorage.init(rootPath = "src/main/webapp/")
        CentralDataStorage.reload()
    }

    @Test
    fun agree_with_everything() {
        val test = MmpiProcess(Gender.Male, Type.Mmpi566)

        allAgree566.forEach {
            test.submitAnswer(it)
        }
        val result = test.calculateResult()
        assertNotNull(result)
    }

    @Test
    fun notCompletedTest() {
        val test = MmpiProcess(Gender.Male, Type.Mmpi566)

        Assertions.assertThrows(RuntimeException::class.java) {
            justFewAnswers.forEach { answer ->
                test.submitAnswer(answer)
            }
            test.calculateResult()
        }
    }

    @Test
    fun oneAfterOneTest() {
        val test = MmpiProcess(Gender.Male, Type.Mmpi377)

        oneAfterOne377.forEach {
            test.submitAnswer(it)
        }
        val result = test.calculateResult()


        assertEquals(3, result.liesScaleL.raw)
        assertEquals(30, result.credibilityScaleF.raw)
        assertEquals(14, result.correctionScaleK.raw)
        assertEquals(20, result.overControlScale1.raw)
        assertEquals(25, result.passivityScale2.raw)
        assertEquals(23, result.labilityScale3.raw)
        assertEquals(31, result.impulsivenessScale4.raw)
        assertEquals(29, result.masculinityScale5.raw)
        assertEquals(14, result.rigidityScale6.raw)
        assertEquals(31, result.anxietyScale7.raw)
        assertEquals(48, result.individualismScale8.raw)
        assertEquals(23, result.optimismScale9.raw)
        assertEquals(31, result.introversionScale0.raw)


        assertEquals(46.0, result.liesScaleL.score.toDouble(), 2.0)
        assertEquals(132.0, result.credibilityScaleF.score.toDouble(), 2.0)
        assertEquals(45.0, result.correctionScaleK.score.toDouble(), 2.0)
        assertEquals(56.0, result.introversionScale0.score.toDouble(), 2.0)
        assertEquals(75.0, result.overControlScale1.score.toDouble(), 2.0)
        assertEquals(62.0, result.passivityScale2.score.toDouble(), 2.0)
        assertEquals(61.0, result.labilityScale3.score.toDouble(), 2.0)
        assertEquals(73.0, result.impulsivenessScale4.score.toDouble(), 2.0)
        assertEquals(72.0, result.masculinityScale5.score.toDouble(), 2.0)
        assertEquals(69.0, result.rigidityScale6.score.toDouble(), 2.0)
        assertEquals(55.0, result.anxietyScale7.score.toDouble(), 2.0)
        assertEquals(97.0, result.individualismScale8.score.toDouble(), 2.0)
        assertEquals(61.0, result.optimismScale9.score.toDouble(), 2.0)
    }

    @Test
    fun checkScales() {
        val scales: MmpiProcess.Scales = CentralDataStorage.mmpi566Data.scales(Gender.Female)
        assertEquals(15, scaleSum(scales.liesScale))
        assertEquals(64, scaleSum(scales.credibilityScale))
        assertEquals(30, scaleSum(scales.correctionScale))
        assertEquals(33, scaleSum(scales.overControlScale1))
        assertEquals(60, scaleSum(scales.passivityScale2))
        assertEquals(60, scaleSum(scales.labilityScale3))
        assertEquals(50, scaleSum(scales.impulsivenessScale4))
        assertEquals(60, scaleSum(scales.masculinityScale5))
        assertEquals(40, scaleSum(scales.rigidityScale6))
        assertEquals(48, scaleSum(scales.anxietyScale7))
        assertEquals(78, scaleSum(scales.individualismScale8))
        assertEquals(46, scaleSum(scales.optimismScale9))
        assertEquals(70, scaleSum(scales.introversionScale))
    }
}

private fun scaleSum(scale: Scale) = scale.yes.size + scale.no.size
