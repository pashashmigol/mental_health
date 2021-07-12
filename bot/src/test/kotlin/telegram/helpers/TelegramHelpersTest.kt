package telegram.helpers

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class TelegramHelpersTest {

    @Test
    fun `full name`() {
        assertEquals("Pasha Shmyhol", formatName("Pasha", "Shmyhol"))
    }

    @Test
    fun `first name`() {
        assertEquals("Shmyhol", formatName(null, "Shmyhol"))
    }

    @Test
    fun `last name`() {
        assertEquals("Pasha", formatName("Pasha", null))
    }
}