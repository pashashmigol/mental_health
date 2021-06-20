package report

import org.w3c.dom.Element
import org.xhtmlrenderer.extend.ReplacedElement
import org.xhtmlrenderer.extend.UserAgentCallback
import org.xhtmlrenderer.layout.LayoutContext
import org.xhtmlrenderer.render.BlockBox
import org.xhtmlrenderer.simple.extend.FormSubmissionListener
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException


class SVGReplacedElementFactory : ChainingReplacedElementFactory() {
    override fun createReplacedElement(
        layoutContext: LayoutContext,
        box: BlockBox,
        uac: UserAgentCallback,
        cssWidth: Int,
        cssHeight: Int
    ): ReplacedElement? {
        val element = box.element
        if ("svg" == element.nodeName) {
            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder: DocumentBuilder = try {
                documentBuilderFactory.newDocumentBuilder()
            } catch (e: ParserConfigurationException) {
                throw RuntimeException(e)
            }
            val svgDocument = documentBuilder.newDocument()
            val svgElement = svgDocument.importNode(element, true) as Element
            svgDocument.appendChild(svgElement)
            return SVGReplacedElement(svgDocument, cssWidth, cssHeight)
        }
        return null
    }

    override fun reset() {}
    override fun remove(e: Element) {}
    override fun setFormSubmissionListener(listener: FormSubmissionListener) {}
}