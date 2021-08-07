package telegram

import Gender
import lucher.LucherColor
import mmpi.MmpiProcess
import models.TypeOfTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class QuizButtonTest {

    @Test
    fun `makeString Gender`() {
        assertEquals("Gender:Male:", QuizButton.GenderAnswer(Gender.Male).makeString())
    }

    @Test
    fun `fromString Gender`() {
        val genderCallback = QuizButton.fromString("Gender:Male:") as QuizButton.GenderAnswer
        assertEquals(Gender.Male, genderCallback.answer)
    }

    @Test
    fun `makeString Mmpi`() {
        val mmpiAnswer = QuizButton.Mmpi(index = 5, answer = MmpiProcess.Answer.Agree)
        assertEquals("Mmpi:Agree:5", mmpiAnswer.makeString())
    }

    @Test
    fun `fromString Mmpi`() {
        val mmpiCallback = QuizButton.fromString("Mmpi:Agree:5") as QuizButton.Mmpi

        assertEquals(
            MmpiProcess.Answer.Agree,
            mmpiCallback.answer
        )
        assertEquals(5, mmpiCallback.index)
    }

    @Test
    fun `makeString Lucher`() {
        val lucherAnswer = QuizButton.Lucher(answer = LucherColor.Blue)
        assertEquals("Lucher:Blue:", lucherAnswer.makeString())
    }

    @Test
    fun `fromString Lucher`() {
        val lucherCallback = QuizButton.fromString("Lucher:Blue:") as QuizButton.Lucher

        assertEquals(
            LucherColor.Blue,
            lucherCallback.answer
        )
    }

    @Test
    fun `makeString NewTestRequest`() {
        val newTestRequest = QuizButton.NewTest(TypeOfTest.Mmpi566)
        assertEquals("NewTestRequest:Mmpi566:", newTestRequest.makeString())
    }

    @Test
    fun `fromString NewTestRequest`() {
        val newTestRequest = QuizButton.fromString("NewTestRequest:Mmpi566:") as QuizButton.NewTest

        assertEquals(
            TypeOfTest.Mmpi566,
            newTestRequest.typeOfTest
        )
    }
}