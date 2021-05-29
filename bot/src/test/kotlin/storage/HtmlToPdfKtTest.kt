package storage

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Timeout
import report.convertHtmlToPdf
import java.io.File
import java.util.concurrent.TimeUnit

internal class HtmlToPdfKtTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `generate pdf files from examples`() {
        val examples = File("src/test/resources/")
        assertTrue(examples.exists())
        assertTrue(examples.isDirectory)
        File("build", "test-results").mkdir()
        File("build/test-results", "pdf").mkdir()

        examples.listFiles { file ->
            try {
                val pdfFile = File("build/test-results/pdf", "${file.nameWithoutExtension}.png")
                pdfFile.createNewFile()
                assertTrue(pdfFile.exists())
                val pdfStream = pdfFile.outputStream()

                convertHtmlToPdf(file, pdfFile)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            true
        }
    }
}