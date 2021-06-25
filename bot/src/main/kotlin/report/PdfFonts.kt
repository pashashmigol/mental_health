package report

import com.itextpdf.text.Font
import com.itextpdf.text.pdf.BaseFont

class PdfFonts(rootPath: String) {
    val base: BaseFont = BaseFont.createFont(
        "$rootPath../resources/main/FreeSans.ttf",
        BaseFont.IDENTITY_H, BaseFont.EMBEDDED
    )
    val bold = Font(base, 14f, Font.BOLD)
    val big = Font(base, 18f, Font.BOLD)
    val normal = Font(base, 14f, Font.NORMAL)
}