package storage

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import report.convertHtmlToPdf2
import java.io.File

internal class HtmlToPdfKtTest {

    @Test
    fun `simple example`() {
        val htmlFile = File("src/test/resources/", "example.html")
        assertTrue(htmlFile.exists())

        val pdfFile = File("build/example.pdf")
        pdfFile.createNewFile()
        assertTrue(pdfFile.exists())

        val pdfStream = pdfFile.outputStream()

        convertHtmlToPdf2(htmlFile, pdfStream)
    }

    @Test
    @Disabled
    fun `mmpi report to pdf`() {
        val htmlFile = File("src/test/resources/", "test_mmpi_report.html")
        assertTrue(htmlFile.exists())

        val pdfFile = File("build/test_mmpi_report.pdf")
        pdfFile.createNewFile()
        assertTrue(pdfFile.exists())

        val pdfStream = pdfFile.outputStream()

        convertHtmlToPdf2(htmlFile, pdfStream)
    }


    @Test
    fun `Lucher report to pdf`() {
        val htmlFile = File("src/test/resources/", "test_lucher_report.html")
        assertTrue(htmlFile.exists())

        val pdfFile = File("build/test_lucher_report.pdf")
        pdfFile.createNewFile()
        assertTrue(pdfFile.exists())

        val pdfStream = pdfFile.outputStream()

        convertHtmlToPdf2(htmlFile, pdfStream)
    }
}