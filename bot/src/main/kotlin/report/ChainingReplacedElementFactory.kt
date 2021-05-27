package report

import org.w3c.dom.Element
import org.xhtmlrenderer.extend.ReplacedElement
import org.xhtmlrenderer.extend.ReplacedElementFactory
import org.xhtmlrenderer.extend.UserAgentCallback
import org.xhtmlrenderer.layout.LayoutContext
import org.xhtmlrenderer.render.BlockBox
import org.xhtmlrenderer.simple.extend.FormSubmissionListener


open class ChainingReplacedElementFactory : ReplacedElementFactory {
    private val replacedElementFactories = mutableListOf<ReplacedElementFactory>()

    fun addReplacedElementFactory(replacedElementFactory: ReplacedElementFactory) {
        replacedElementFactories.add(0, replacedElementFactory)
    }

    override fun createReplacedElement(
        layoutContext: LayoutContext,
        box: BlockBox,
        uac: UserAgentCallback,
        cssWidth: Int,
        cssHeight: Int
    ): ReplacedElement? {
        for (replacedElementFactory in replacedElementFactories) {
            val element = replacedElementFactory.createReplacedElement(
                layoutContext, box, uac, cssWidth, cssHeight
            )
            if (element != null) {
                return element
            }
        }
        return null
    }

    override fun reset() {
        for (replacedElementFactory in replacedElementFactories) {
            replacedElementFactory.reset()
        }
    }

    override fun remove(e: Element) {
        for (replacedElementFactory in replacedElementFactories) {
            replacedElementFactory.remove(e)
        }
    }

    override fun setFormSubmissionListener(listener: FormSubmissionListener) {
        for (replacedElementFactory in replacedElementFactories) {
            replacedElementFactory.setFormSubmissionListener(listener)
        }
    }
}