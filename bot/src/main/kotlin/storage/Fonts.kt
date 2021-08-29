package storage

import report.PdfFonts

object Fonts {
    private val fonts = PdfFonts()

    val base = fonts.base
    val bold = fonts.bold
    val big = fonts.big
    val normal = fonts.normal
}