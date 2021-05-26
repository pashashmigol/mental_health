package report

import com.lowagie.text.pdf.PdfContentByte
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.print.PrintTranscoder
import org.w3c.dom.Document
import org.xhtmlrenderer.css.style.CalculatedStyle
import org.xhtmlrenderer.layout.LayoutContext
import org.xhtmlrenderer.pdf.ITextOutputDevice
import org.xhtmlrenderer.pdf.ITextReplacedElement
import org.xhtmlrenderer.render.BlockBox
import org.xhtmlrenderer.render.RenderingContext
import java.awt.Point
import java.awt.print.PageFormat
import java.awt.print.Paper


class SVGReplacedElement(private val svg: Document, private val cssWidth: Int, private val cssHeight: Int) :
    ITextReplacedElement {
    private val location = Point(0, 0)
    override fun detach(c: LayoutContext) {}
    override fun getBaseline(): Int {
        return 0
    }

    override fun getIntrinsicWidth(): Int {
        return cssWidth
    }

    override fun getIntrinsicHeight(): Int {
        return cssHeight
    }

    override fun hasBaseline(): Boolean {
        return false
    }

    override fun isRequiresInteractivePaint(): Boolean {
        return false
    }

    override fun getLocation(): Point {
        return location
    }

    override fun setLocation(x: Int, y: Int) {
        location.x = x
        location.y = y
    }

    override fun paint(
        renderingContext: RenderingContext, outputDevice: ITextOutputDevice,
        blockBox: BlockBox
    ) {
        val cb: PdfContentByte = outputDevice.writer.directContent
        val width = (cssWidth / outputDevice.dotsPerPoint)
        val height = (cssHeight / outputDevice.dotsPerPoint)
        val template = cb.createTemplate(width, height)
        val g2d = template.createGraphics(width, height)
        val prm = PrintTranscoder()
        val ti = TranscoderInput(svg)
        prm.transcode(ti, null)
        val pg = PageFormat()
        val pp = Paper()
        pp.setSize(width.toDouble(), height.toDouble())
        pp.setImageableArea(0.0, 0.0, width.toDouble(), height.toDouble())
        pg.paper = pp
        prm.print(g2d, pg, 0)
        g2d.dispose()
        val page = renderingContext.page
        var x = (blockBox.absX + page.getMarginBorderPadding(renderingContext, CalculatedStyle.LEFT)).toFloat()
        var y = (page.bottom - (blockBox.absY + cssHeight) + page.getMarginBorderPadding(
            renderingContext, CalculatedStyle.BOTTOM
        )).toFloat()
        x /= outputDevice.dotsPerPoint
        y /= outputDevice.dotsPerPoint
        cb.addTemplate(template, x, y)
    }
}