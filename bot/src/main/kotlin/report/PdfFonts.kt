package report

import com.itextpdf.text.Font
import com.itextpdf.text.pdf.BaseFont
import telegram.LaunchMode

class PdfFonts(launchMode: LaunchMode) {

    private val path = when(launchMode){
        LaunchMode.LOCAL,LaunchMode.TESTS -> "${launchMode.rootPath}/resources/FreeSans.ttf"
        LaunchMode.APP_ENGINE -> "${launchMode.rootPath}FreeSans.ttf"
    }

    val base: BaseFont = BaseFont.createFont(
        path,
        BaseFont.IDENTITY_H,
        BaseFont.EMBEDDED
    )
    val bold = Font(base, 14f, Font.BOLD)
    val big = Font(base, 18f, Font.BOLD)
    val normal = Font(base, 14f, Font.NORMAL)
}