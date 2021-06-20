package mmpi.report

import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import mmpi.MmpiAnswers
import mmpi.MmpiProcess
import models.Question
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.JPEGTranscoder
import storage.CentralDataStorage.pdfFonts


fun pdfReportMmpi(
    questions: List<Question>,
    answers: MmpiAnswers,
    result: MmpiProcess.Result
): ByteArray {
    val document = Document()

    val outputStream = ByteArrayOutputStream()
    PdfWriter.getInstance(document, outputStream)

    document.isMarginMirroring = true
    document.open()

    document.add(Paragraph(answers.user.name, pdfFonts().big))
    addChart(result, document)

    result.scalesToShow.forEach {
        val scaleParagraph = Paragraph()
        scaleParagraph.spacingAfter = 16f
        scaleParagraph.add(Chunk("    ${it.name} - ${it.score}", pdfFonts().bold))

        if (it.description.isNotEmpty()) {
            scaleParagraph.add(Chunk(": ", pdfFonts().bold))
            scaleParagraph.add(Chunk(it.description, pdfFonts().normal))
        }
        document.add(scaleParagraph)
    }

    val table = PdfPTable(2)
    table.widthPercentage = 100f
    table.spacingBefore = 32f
    table.setWidths(arrayOf(70f, 30f).toFloatArray())

    questions.zip(answers.answersList).forEach {
        val (question, answer) = it
        table.addCell(createCell(question.text))
        table.addCell(createCell(answer.text))
    }
    document.add(table)

    document.close()
    return outputStream.toByteArray()
}

private fun createCell(text: String): PdfPCell {
    val phrase = Phrase(text, pdfFonts().normal)

    return PdfPCell(phrase).apply {
        horizontalAlignment = Element.ALIGN_LEFT
        paddingLeft = 4f
        paddingRight = 16f
        paddingTop = 4f
        paddingBottom = 12f
    }
}

private fun addChart(mmpiResult: MmpiProcess.Result, document: Document) {
    val chartContainer = Paragraph()
    val img: Image = createChart(mmpiResult)
    chartContainer.add(img)
    chartContainer.spacingBefore = 60f
    document.add(chartContainer)
}

fun createChart(
    mmpiResult: MmpiProcess.Result
): Image {
    val svgStr = chartFor(mmpiResult)
    val inputSvgImage = TranscoderInput(svgStr.byteInputStream(Charsets.UTF_8))

    val outputStream = ByteArrayOutputStream()

    val transcoderOutput = TranscoderOutput(outputStream)
    val jpegTranscoder = JPEGTranscoder()

    jpegTranscoder.transcode(inputSvgImage, transcoderOutput)

    val image = Jpeg(outputStream.toByteArray(), 405f, 405f)

    outputStream.flush()
    outputStream.close()

    return image
}