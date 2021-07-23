package telegram

import Gender
import lucher.LucherColor
import mmpi.MmpiProcess
import models.TypeOfTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class CallbackTest {

    @Test
    fun `makeString Gender`() {
        assertEquals("Gender:Male:", Callback.GenderAnswer(Gender.Male).makeString())
    }

    @Test
    fun `fromString Gender`() {
        val genderCallback = Callback.fromString("Gender:Male:") as Callback.GenderAnswer
        assertEquals(Gender.Male, genderCallback.answer)
    }

    @Test
    fun `makeString Mmpi`() {
        val mmpiAnswer = Callback.MmpiAnswer(index = 5, answer = MmpiProcess.Answer.Agree)
        assertEquals("Mmpi:Agree:5", mmpiAnswer.makeString())
    }

    @Test
    fun `fromString Mmpi`() {
        val mmpiCallback = Callback.fromString("Mmpi:Agree:5") as Callback.MmpiAnswer

        assertEquals(
            MmpiProcess.Answer.Agree,
            mmpiCallback.answer
        )
        assertEquals(5, mmpiCallback.index)
    }

    @Test
    fun `makeString Lucher`() {
        val lucherAnswer = Callback.LucherAnswer(answer = LucherColor.Blue)
        assertEquals("Lucher:Blue:", lucherAnswer.makeString())
    }

    @Test
    fun `fromString Lucher`() {
        val lucherCallback = Callback.fromString("Lucher:Blue:") as Callback.LucherAnswer

        assertEquals(
            LucherColor.Blue,
            lucherCallback.answer
        )
    }

    @Test
    fun `makeString NewTestRequest`() {
        val newTestRequest = Callback.NewTestRequest(TypeOfTest.Mmpi566)
        assertEquals("NewTestRequest:Mmpi566:", newTestRequest.makeString())
    }

    @Test
    fun `fromString NewTestRequest`() {
        val newTestRequest = Callback.fromString("NewTestRequest:Mmpi566:") as Callback.NewTestRequest

        assertEquals(
            TypeOfTest.Mmpi566,
            newTestRequest.typeOfTest
        )
    }
}