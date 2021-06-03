package mmpi.report

import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.text.*
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfWriter
import mmpi.MmpiAnswers
import mmpi.MmpiProcess
import models.Question
import models.User
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.JPEGTranscoder
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream


private val baseFont: BaseFont = BaseFont.createFont(
    "src/main/resources/FreeSans.ttf",
    BaseFont.IDENTITY_H, BaseFont.EMBEDDED
)
private val boldFont = Font(baseFont, 14f, Font.BOLD)
private val bigFont = Font(baseFont, 18f, Font.BOLD)
private val normalFont = Font(baseFont, 14f, Font.NORMAL)

fun generatePdf(
    questions: List<Question>,
    answers: MmpiAnswers,
    result: MmpiProcess.Result,
    pdfStream: OutputStream
) {
    val document = Document()
    PdfWriter.getInstance(document, pdfStream)

    document.isMarginMirroring = true
    document.open()

    document.add(Paragraph(answers.user.name, bigFont))
    addChart(result, document)

    result.scalesToShow.forEach {

        val scaleParagraph = Paragraph()
        scaleParagraph.add(Chunk("    ${it.name} - ${it.score}", boldFont))

        if(it.description.isNotEmpty()){
            scaleParagraph.add(Chunk(": ", boldFont))
            scaleParagraph.add(Chunk(it.description, normalFont))
        }
        document.add(scaleParagraph)
    }
    document.close()
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