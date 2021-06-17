package mmpi.report

import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.text.*
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import mmpi.MmpiAnswers
import mmpi.MmpiProcess
import models.Question
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.JPEGTranscoder


private val baseFont: BaseFont = BaseFont.createFont(
    "src/main/resources/FreeSans.ttf",
    BaseFont.IDENTITY_H, BaseFont.EMBEDDED
)
private val boldFont = Font(baseFont, 14f, Font.BOLD).apply {
    color = BaseColor.BLACK
}
private val bigFont = Font(baseFont, 18f, Font.BOLD).apply {
    color = BaseColor.BLACK
}
private val normalFont = Font(baseFont, 14f, Font.NORMAL).apply {
    color = BaseColor.BLACK
}

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

    document.add(Paragraph(answers.user.name, bigFont))
    addChart(result, document)

    result.scalesToShow.forEach {
        val scaleParagraph = Paragraph()
        scaleParagraph.spacingAfter = 16f
        scaleParagraph.add(Chunk("    ${it.name} - ${it.score}", boldFont))

        if (it.description.isNotEmpty()) {
            scaleParagraph.add(Chunk(": ", boldFont))
            scaleParagraph.add(Chunk(it.description, normalFont))
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
    val phrase = Phrase(text, normalFont)
    val pdfPCell = PdfPCell(phrase)
    pdfPCell.horizontalAlignment = Element.ALIGN_LEFT
    pdfPCell.paddingLeft = 4f
    pdfPCell.paddingRight = 16f
    pdfPCell.paddingTop = 4f
    pdfPCell.paddingBottom = 12f
    return pdfPCell
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