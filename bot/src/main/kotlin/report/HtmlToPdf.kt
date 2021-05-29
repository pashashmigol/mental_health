package report

import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.Paths


fun convertHtmlToPdf(
    htmlFile: File,
    pdfFile: File
) {
    val svgUriInput = Paths.get("src/main/resources/steps.svg").toUri().toURL().toString()
    val inputSvgImage = TranscoderInput(svgUriInput)

    val outputStream: OutputStream = pdfFile.outputStream()

    val transcoderOutput = TranscoderOutput(outputStream)
    val pngTranscoder = PNGTranscoder()

    pngTranscoder.transcode(inputSvgImage, transcoderOutput)
    outputStream.flush()
    outputStream.close()
}