package lucher.report

import com.itextpdf.text.*
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfWriter
import lucher.LucherAnswers
import lucher.LucherResult
import java.io.ByteArrayOutputStream
import java.io.OutputStream


private val baseFont: BaseFont = BaseFont.createFont(
    "src/main/resources/FreeSans.ttf",
    BaseFont.IDENTITY_H, BaseFont.EMBEDDED
)

private val boldFont = Font(baseFont, 14f, Font.BOLD)
private val bigFont = Font(baseFont, 18f, Font.BOLD)
private val normalFont = Font(baseFont, 14f, Font.NORMAL)

fun pdfReportLucher(
    answers: LucherAnswers,
    result: LucherResult
): String {
    val document = Document()

    val outputStream = ByteArrayOutputStream()
    PdfWriter.getInstance(document, outputStream)

    document.isMarginMirroring = true
    document.open()
    document.add(Paragraph(answers.user.name, bigFont))
    document.close()

    return outputStream.toString(Charsets.UTF_8)
}