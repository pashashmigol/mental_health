package mmpi

import models.TypeOfTest
import models.size
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.kodein.di.instance
import testDI

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MmpiProcessTest {

    private val mmpiData: MmpiData by testDI.instance(TypeOfTest.Mmpi566)

    @Test
    fun `simple run`() {
        val process = MmpiProcess(Gender.Male, TypeOfTest.Mmpi566, mmpiData)

        repeat(TypeOfTest.Mmpi566.size) { index ->
            assertTrue(process.hasNextQuestion(), "index = $index")

            val question = process.nextQuestion()

            assertEquals(index, question.index, "index = $index")
            process.submitAnswer(index, MmpiProcess.Answer.Agree)
        }

        val randomIndex = 20
        process.submitAnswer(randomIndex, MmpiProcess.Answer.Disagree)

        assertEquals(MmpiProcess.Answer.Disagree, process.answers[randomIndex])

        process.calculateResult()
    }
}