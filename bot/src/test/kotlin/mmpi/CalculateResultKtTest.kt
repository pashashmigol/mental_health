package mmpi

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CalculateResultKtTest {

    @BeforeEach
    fun setup() {
        CurrentQuestionsProvider.initGoogleSheetsProvider(rootPath = "src/main/webapp/")
    }

    @Test
    fun agree_with_everything() {
        val test = Mmpi566()

        allAgree.forEach {
            test.submitAnswer(it)
        }
        val result = test.calculateResult()
        assertNotNull(result)
    }

    @Test
    fun notCompletedTest() {
        val test = Mmpi566()

        Assertions.assertThrows(RuntimeException::class.java) {
            justFewAnswers.forEach {
                test.submitAnswer(it)
            }
            test.calculateResult()
        }
    }

    @Test
    fun checkScales() {
        assertEquals(15, scaleSum(LiesScaleL))
        assertEquals(64, scaleSum(CredibilityScaleF))
        assertEquals(30, scaleSum(CorrectionScaleK))
        assertEquals(33, scaleSum(OverControlScale1))
        assertEquals(60, scaleSum(PassivityScale2))
        assertEquals(60, scaleSum(LabilityScale3))
        assertEquals(50, scaleSum(ImpulsivenessScale4))
        assertEquals(60, scaleSum(MasculinityScale5M))
        assertEquals(60, scaleSum(FemininityScale5F))
        assertEquals(40, scaleSum(RigidityScale6))
        assertEquals(48, scaleSum(AnxietyScale7))
        assertEquals(78, scaleSum(IndividualismScale8))
        assertEquals(46, scaleSum(OptimismScale9))
        assertEquals(70, scaleSum(IntroversionScale0))
    }
}

private fun scaleSum(scale: Scale) = scale.yes.size + scale.no.size
