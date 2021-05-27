package report

import com.lowagie.text.pdf.BaseFont
import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.File
import java.io.OutputStream


fun convertHtmlToPdf(
    htmlFile: File,
    pdfStream: OutputStream
) {
    pdfStream.use { stream ->
        val renderer = ITextRenderer()

        val chainingReplacedElementFactory = ChainingReplacedElementFactory()
        chainingReplacedElementFactory.addReplacedElementFactory(renderer.sharedContext.replacedElementFactory)
        chainingReplacedElementFactory.addReplacedElementFactory(SVGReplacedElementFactory())
        renderer.sharedContext.replacedElementFactory = chainingReplacedElementFactory

        renderer.fontResolver.addFont(
            "Symbol",
//            "src/main/resources/Soviet-M4Kw.ttf",
            "UTF-8",
            BaseFont.NOT_EMBEDDED
        )

        renderer.setDocument(htmlFile)
        renderer.layout()
        renderer.createPDF(stream)
        stream.close()
    }
}