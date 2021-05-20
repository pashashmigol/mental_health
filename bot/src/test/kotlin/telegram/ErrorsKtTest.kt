package telegram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.RuntimeException

internal class ErrorsKtTest {

    @Test
    fun `pass all nulls`() {

        val actual = formatMessage(
            message = null,
            exception = null
        )

        assertEquals("", actual)
    }

    @Test
    fun `pass null for message`() {

        val actual = formatMessage(
            message = null,
            exception = RuntimeException("test error message")
        )

        assertTrue(actual.startsWith("test error message"))
    }


    @Test
    fun `pass null for exception`() {

        val actual = formatMessage(
            message = "test error message",
            exception = null
        )

        assertEquals("test error message", actual)
    }

    @Test
    fun `pass both exception and error message`() {

        val actual = formatMessage(
            message = "test error message",
            exception = RuntimeException("test exception message")
        )

        assertTrue(actual.contains("test error message"))
        assertTrue(actual.contains("test exception message"))
    }
}