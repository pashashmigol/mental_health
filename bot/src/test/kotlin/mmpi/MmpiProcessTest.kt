package mmpi

import models.Type
import models.size
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import storage.CentralDataStorage
import telegram.LaunchMode

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MmpiProcessTest {

    @BeforeAll
    fun init() {
        CentralDataStorage.init(LaunchMode.TESTS.rootPath)
    }

    @Test
    fun `simple run`() {
        val process = MmpiProcess(Gender.Male, Type.Mmpi566)

        repeat(Type.Mmpi566.size) { index ->
            assertTrue(process.hasNextQuestion(), "index = $index")

            val question = process.nextQuestion()
            assertEquals(index, question.index, "index = $index")

            assertTrue(process.isItLastAskedQuestion(index), "index = $index")
            assertFalse(process.isItLastAskedQuestion(index - 1), "index = $index")
            process.submitAnswer(index, MmpiProcess.Answer.Agree)
            assertTrue(process.isItLastAskedQuestion(index), "index = $index")
            assertFalse(process.isItLastAskedQuestion(index + 1), "index = $index")
        }

        val randomIndex = 20
        process.submitAnswer(randomIndex, MmpiProcess.Answer.Disagree)

        assertEquals(MmpiProcess.Answer.Disagree, process.answers[randomIndex])

        process.calculateResult()
    }
}