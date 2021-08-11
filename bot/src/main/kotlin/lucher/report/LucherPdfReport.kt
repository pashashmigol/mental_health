package lucher.report

import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import lucher.*
import lucher.LucherElement
import storage.CentralDataStorage.pdfFonts
import storage.CentralDataStorage.string
import java.io.ByteArrayOutputStream


fun pdfReportLucher(
    answers: LucherAnswersContainer,
    result: LucherResult
): ByteArray {
    val document = Document()

    val outputStream = ByteArrayOutputStream()
    PdfWriter.getInstance(document, outputStream)
    document.isMarginMirroring = true
    document.open()

    Paragraph(answers.user.name, pdfFonts().big).let { document.add(it) }

    createNumbersRow(string("first_round"), answers.firstRound).let { document.add(it) }
    createNumbersRow(string("second_round"), answers.secondRound).let { document.add(it) }

    descriptionForPairs(string("stable_pairs"), result.stablePairs).let { document.add(it) }
    descriptionForPairs(string("broken_pairs"), result.brokenPairs).let { document.add(it) }
    descriptionForPairs(string("contraversed_pairs"), result.contraversedPairs).let { document.add(it) }

    anxietyDescription(result).let { document.add(it) }

    document.close()
    return outputStream.toByteArray()
}


private fun createNumbersRow(header: String, colors: List<LucherColor>): Paragraph {
    return Paragraph().apply {
        spacingBefore = 24f
        keepTogether = true

        add(
            Phrase().apply {
                add(Chunk(header, pdfFonts().bold))
                add(Chunk.TABBING)
                colors.forEach { color ->
                    add(Chunk.TABBING)
                    add(createColorBox(color))
                }
            }
        )
    }
}

private fun anxietyDescription(
    result: LucherResult
): Paragraph = Paragraph().apply {
    spacingBefore = 24f
    keepTogether = true

    add(Chunk(string("anxiety"), pdfFonts().big))
    add(Chunk.NEWLINE)

    add(
        Chunk("${string("first_round")}: ${result.firstRoundAnxiety}", pdfFonts().normal)
    )
    add(Chunk.NEWLINE)

    add(
        Chunk("${string("second_round")}: ${result.secondRoundAnxiety}", pdfFonts().normal)
    )
}

private fun descriptionForPairs(
    header: String,
    pairs: Map<LucherElement, String>
): Paragraph {
    if (pairs.isEmpty()) {
        return Paragraph()
    }
    val paragraph = Paragraph().apply {
        keepTogether = true
    }
    Paragraph(header, pdfFonts().big).let {
        paragraph.add(Chunk.NEWLINE)
        paragraph.add(Chunk.NEWLINE)
        paragraph.add(it)
    }
    val table = PdfPTable(2).apply {
        widthPercentage = 95f
        spacingBefore = 12f
        setWidths(arrayOf(15f, 85f).toFloatArray())
        paragraph.add(this)
    }
    pairs.forEach { (element, description) ->
        createNumberCell(element).let {
            table.addCell(it)
        }
        createDescriptionCell(description).let {
            table.addCell(it)
        }
    }
    return paragraph
}

private fun createNumberCell(element: LucherElement): PdfPCell {
    val pdfPCell = PdfPCell().apply {
        horizontalAlignment = Element.ALIGN_LEFT
        verticalAlignment = Element.ALIGN_TOP
        border = PdfPCell.NO_BORDER
        paddingLeft = 4f
        paddingRight = 4f
        paddingTop = 16f
        paddingBottom = 36f
    }
    val phrase = Phrase()
    when (element) {
        is LucherElement.Pair -> {
            createColorBox(element.firstColor.color).let {
                phrase.add(it)
            }
            Chunk.TABBING.let {
                phrase.add(it)
            }
            createColorBox(element.secondColor.color).let {
                phrase.add(it)
            }
        }
        is LucherElement.Single -> {
            createColorBox(element.color.color).let {
                phrase.add(it)
            }
        }
    }
    pdfPCell.addElement(phrase)
    return pdfPCell
}

private fun createColorBox(color: LucherColor): Chunk {
    val font = Font(pdfFonts().base, 25.0f, Font.BOLD, BaseColor.WHITE)
    val chunk = Chunk(color.index.toString(), font)
    chunk.setBackground(
        BaseColor(color.toARGB()), 10f, 5f, 10f, 5f
    )
    return chunk
}

private fun createDescriptionCell(description: String): PdfPCell {
    val phrase = Phrase(description, pdfFonts().normal)

    return PdfPCell(phrase).apply {
        horizontalAlignment = Element.ALIGN_LEFT
        border = PdfPCell.NO_BORDER
        paddingLeft = 4f
        paddingRight = 4f
        paddingTop = 4f
        paddingBottom = 36f
    }
}