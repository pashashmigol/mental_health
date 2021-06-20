package report

import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.text.*
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfWriter
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.JPEGTranscoder
import java.io.File
import java.io.OutputStream
import java.nio.file.Paths


fun convertHtmlToPdf(
    htmlFile: File,
    pdfStream: OutputStream
) {
    val document = Document()
    PdfWriter.getInstance(document, pdfStream)

    document.open()


//    val chunk = Paragraph("Трали-вали WWW gvhgvvjghvjghvjghvjhgvjghv jhgv ghvhgjv jhgbuyiuygbyugbuygbygu")
//    document.add(chunk)

//    val img: Image = convertSVGToPng()
//    document.add(img)

    val bf: BaseFont = BaseFont.createFont( "src/main/resources/FreeSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
    val font = Font(bf, 14f, Font.NORMAL)

    document.add(Paragraph("WWW - Привет", font))
    document.close()

    document.close()
}


fun convertSVGToPng(): Image {
    val svgUriInput = Paths.get("src/main/resources/steps.svg").toUri().toURL().toString()
    val inputSvgImage = TranscoderInput(svgUriInput)

    val outputStream = ByteArrayOutputStream()

    val transcoderOutput = TranscoderOutput(outputStream)
    val jpegTranscoder = JPEGTranscoder()

    jpegTranscoder.transcode(inputSvgImage, transcoderOutput)

    val image = Jpeg(outputStream.toByteArray(), 300f, 300f)

    outputStream.flush()
    outputStream.close()

    return image
}