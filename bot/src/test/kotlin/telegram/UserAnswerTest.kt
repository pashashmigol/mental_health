package telegram

import Gender
import lucher.LucherColor
import mmpi.MmpiProcess
import models.TypeOfTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class UserAnswerTest {

    @Test
    fun `makeString Gender`() {
        assertEquals("Gender:Male:", UserAnswer.GenderAnswer(Gender.Male).makeString())
    }

    @Test
    fun `fromString Gender`() {
        val genderCallback = UserAnswer.fromString("Gender:Male:") as UserAnswer.GenderAnswer
        assertEquals(Gender.Male, genderCallback.answer)
    }

    @Test
    fun `makeString Mmpi`() {
        val mmpiAnswer = UserAnswer.Mmpi(index = 5, answer = MmpiProcess.Answer.Agree)
        assertEquals("Mmpi:Agree:5", mmpiAnswer.makeString())
    }

    @Test
    fun `fromString Mmpi`() {
        val mmpiCallback = UserAnswer.fromString("Mmpi:Agree:5") as UserAnswer.Mmpi

        assertEquals(
            MmpiProcess.Answer.Agree,
            mmpiCallback.answer
        )
        assertEquals(5, mmpiCallback.index)
    }

    @Test
    fun `makeString Lucher`() {
        val lucherAnswer = UserAnswer.Lucher(answer = LucherColor.Blue)
        assertEquals("Lucher:Blue:", lucherAnswer.makeString())
    }

    @Test
    fun `fromString Lucher`() {
        val lucherCallback = UserAnswer.fromString("Lucher:Blue:") as UserAnswer.Lucher

        assertEquals(
            LucherColor.Blue,
            lucherCallback.answer
        )
    }

    @Test
    fun `makeString NewTestRequest`() {
        val newTestRequest = UserAnswer.NewTest(TypeOfTest.Mmpi566)
        assertEquals("NewTestRequest:Mmpi566:", newTestRequest.makeString())
    }

    @Test
    fun `fromString NewTestRequest`() {
        val newTestRequest = UserAnswer.fromString("NewTestRequest:Mmpi566:") as UserAnswer.NewTest

        assertEquals(
            TypeOfTest.Mmpi566,
            newTestRequest.typeOfTest
        )
    }

    @Test
    fun `fromString Skip`() {
        val newTestRequest = UserAnswer.fromString("Skip::")
        assertTrue(newTestRequest is UserAnswer.Skip)
    }
}