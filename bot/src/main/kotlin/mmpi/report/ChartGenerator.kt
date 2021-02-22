package mmpi.report

import com.github.nwillc.ksvg.RenderMode
import com.github.nwillc.ksvg.elements.Container
import com.github.nwillc.ksvg.elements.SVG
import mmpi.MmpiProcess
import java.io.StringWriter


fun chartFor(result: MmpiProcess.Result): String {
    val ys = result.scalesToShow
        .map { it.score }

    val svg = mmpiChartTemplate(605, ys)

    val writer = StringWriter()
    svg.render(writer, RenderMode.INLINE)
    return writer.toString()
}

fun mmpiChartTemplate(desiredSize: Int, values: List<Int>): SVG {
    val padding = 100
    val maxScaleValue = 120
    val step = desiredSize / 12
    val size = step * 12
    val xOffset = 25
    val yOffset = 5

    val ys = values
        .map { it.toFloat() }
        .map { size.toFloat() * (1 - it / maxScaleValue) }
        .map { it.toInt() }

    return SVG.svg {
        width = "${desiredSize + padding}px"
        height = "${desiredSize + padding}px"
        viewBox = "0 0 $width $height"

        g {
            id = "graph"
            attributes["stroke"] = "none"
            attributes["stroke-width"] = "1"
            attributes["fill"] = "none"
            attributes["fill-rule"] = "evenodd"
            attributes["transform"] = "translate(40.000000, 56.000000)"

            g {
                attributes["fill"] = "#000000"
                drawVerticalBars(xOffset, size, step, yOffset)
                drawHorizontalBars(yOffset, size, step, xOffset)
            }

            drawCurve(xOffset, size, step, ys, yOffset, 0, 2)
            drawCurve(xOffset, size, step, ys, yOffset, 3, 12)
        }
    }
}

private fun Container.drawCurve(
    xOffset: Int,
    size: Int,
    step: Int,
    ys: List<Int>,
    yOffset: Int,
    from: Int,
    to: Int
) {
    val verticalBars = xOffset..(xOffset + size) step step

    (verticalBars zip ys)
        .subList(from, to + 1)
        .zipWithNext()
        .forEach {
            val (ax, ay) = it.first
            val (bx, by) = it.second
            line {
                x1 = ax.toString()
                y1 = (ay + yOffset).toString()
                x2 = bx.toString()
                y2 = (by + yOffset).toString()
                stroke = "#ff0000"
                strokeWidth = "3"
            }
        }
}

private fun Container.drawHorizontalBars(
    yOffset: Int,
    size: Int,
    step: Int,
    xOffset: Int
) {
    (yOffset..(yOffset + size) step step).forEach { Y ->
        rect {
            x = xOffset.toString()
            y = Y.toString()
            width = size.toString()
            height = "1"
        }
    }

    val marks = arrayOf(
        "0", "10", "20", "30", "40", "50", "60",
        "70", "80", "90", "100", "110", "120"
    ).iterator()

    for (Y in size downTo 0 step step) {
        text {
            fontFamily = "Roboto-Medium, Roboto"
            fontSize = "12"
            attributes["font-weight"] = "400"
            x = "2"
            y = (Y + yOffset * 2).toString()
            body = marks.next()
        }
    }
}

private fun Container.drawVerticalBars(
    xOffset: Int,
    size: Int,
    step: Int,
    yOffset: Int
) {
    (xOffset..(xOffset + size) step step).forEach { X ->
        rect {
            x = X.toString()
            y = yOffset.toString()
            width = "1"
            height = size.toString()
        }
    }
    val scales = arrayOf(
        "L", "F", "K", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"
    ).iterator()

    val verticalBars = xOffset..(xOffset + size) step step

    for (X in verticalBars) {
        text {
            fontFamily = "Roboto-Medium, Roboto"
            fontSize = "12"
            attributes["font-weight"] = "400"
            x = (X - 2).toString()
            y = (size + 20).toString()
            body = scales.next()
        }
    }
}
