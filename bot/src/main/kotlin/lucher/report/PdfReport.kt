package lucher.report

import com.itextpdf.text.*
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfWriter
import lucher.LucherAnswers
import lucher.LucherResult
import models.User
import java.io.InputStream
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
    user: User,
    answers: LucherAnswers,
    result: LucherResult
): InputStream {
    val document = Document()

    val out = PipedOutputStream()
    val input = PipedInputStream()

    out.connect(input)

    PdfWriter.getInstance(document, out)

    document.isMarginMirroring = true
    document.open()

    document.add(Paragraph(user.name, bigFont))
//    addChart(result, document)

//    result.scalesToShow.forEach {
//
//        val scaleParagraph = Paragraph()
//        scaleParagraph.add(Chunk("    ${it.name} - ${it.score}", boldFont))
//
//        if(it.description.isNotEmpty()){
//            scaleParagraph.add(Chunk(": ", boldFont))
//            scaleParagraph.add(Chunk(it.description, normalFont))
//        }
//        document.add(scaleParagraph)
//    }

    document.close()
    document.close()

    return input
}