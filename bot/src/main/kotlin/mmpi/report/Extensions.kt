package mmpi.report

import kotlinx.html.BODY
import kotlinx.html.HtmlTagMarker


@HtmlTagMarker
fun BODY.chart(svg: String) =
    consumer.onTagContentUnsafe {
        raw(svg)
    }


