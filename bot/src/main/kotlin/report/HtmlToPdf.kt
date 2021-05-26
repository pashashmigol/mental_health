package report

import org.fit.pdfdom.resource.ImageResource
import org.xhtmlrenderer.pdf.ITextOutputDevice
import org.xhtmlrenderer.pdf.ITextRenderer
import org.xhtmlrenderer.pdf.ITextUserAgent
import java.awt.Image
import java.io.File
import java.io.OutputStream


//fun convertHtmlToPdf(
//    htmlStream: InputStream,
//    pdfStream: OutputStream
//) {
//    val document = Document()
//    val writer: PdfWriter = PdfWriter.getInstance(
//        document,
//        pdfStream
//    )
//    document.open()
//    XMLWorkerHelper.getInstance().parseXHtml(
//        writer, document,
//        htmlStream
//    )
//    document.close()
//}

fun convertHtmlToPdf2(
    htmlFile: File,
    pdfStream: OutputStream
) {
    pdfStream.use { stream ->
        val renderer = ITextRenderer()

        val chainingReplacedElementFactory = ChainingReplacedElementFactory()
        chainingReplacedElementFactory.addReplacedElementFactory(renderer.sharedContext.replacedElementFactory)
        chainingReplacedElementFactory.addReplacedElementFactory(SVGReplacedElementFactory())
        renderer.sharedContext.replacedElementFactory = chainingReplacedElementFactory

        renderer.setDocument(htmlFile)
        renderer.layout()
        renderer.createPDF(stream)
        stream.close()
    }
}