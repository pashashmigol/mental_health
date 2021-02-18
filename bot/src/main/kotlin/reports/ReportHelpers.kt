package reports

import com.github.nwillc.ksvg.RenderMode
import com.github.nwillc.ksvg.elements.SVG
import mmpi.MmpiProcess
import java.io.StringWriter


fun chartFor(result: MmpiProcess.Result): String {
    val svg = mmpiChartTemplate()

    val writer = StringWriter()
    svg.render(writer, RenderMode.FILE)
    return writer.toString()
}

fun mmpiChartTemplate() = SVG.svg {
    width = "369px"
    height = "389px"
    viewBox = "0 0 369 389"

    g {
        id = "graph"
        attributes["stroke"] = "none"
        attributes["stroke-width"] = "1"
        attributes["fill"] = "none"
        attributes["fill-rule"] = "evenodd"

        g {
            attributes["transform"] = "translate(40.000000, 56.000000)"
            attributes["fill"] = "#000000"

            for (X in 25..277 step 21) {
                rect {
                    x = X.toString()
                    y = "5"
                    width = "1"
                    height = "253"
                }
            }
            for (Y in 257 downTo 5 step 21) {
                rect {
                    x = "25"
                    y = Y.toString()
                    width = "253"
                    height = "1"
                }
            }
            val scales = arrayOf("L", "F", "K", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0").iterator()
            for (X in 22..275 step 21) {
                text {
                    fontFamily = "Roboto-Medium, Roboto"
                    fontSize = "12"
                    attributes["font-weight"] = "400"
                    x = X.toString()
                    y = "273"
                    body = scales.next()
                }
            }
            val numbers =
                arrayOf("0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120").iterator()
            for (Y in 261 downTo 0 step 21) {
                text {
                    fontFamily = "Roboto-Medium, Roboto"
                    fontSize = "12"
                    attributes["font-weight"] = "400"
                    x = "2"
                    y = Y.toString()
                    body = numbers.next()
                }
            }
        }

    }
}

// <text id="0" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="14.1796875" y="261">0</tspan>
// </text>
// <text id="10" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="7.359375" y="241">10</tspan>
// </text>
// <text id="20" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="7.359375" y="220">20</tspan>
// </text>
// <text id="30" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="7.359375" y="199">30</tspan>
// </text>
// <text id="40" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="7.359375" y="177">40</tspan>
// </text>
// <text id="50" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="7.359375" y="156">50</tspan>
// </text>
// <text id="60" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="7.359375" y="135">60</tspan>
// </text>
// <text id="70" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="7.359375" y="114">70</tspan>
// </text>
// <text id="80" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="7.359375" y="94">80</tspan>
// </text>
// <text id="90" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="7.359375" y="73">90</tspan>
// </text>
// <text id="100" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="0.5390625" y="51">100</tspan>
// </text>
// <text id="110" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="0.5390625" y="31">110</tspan>
// </text>
// <text id="120" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="0.5390625" y="11">120</tspan>
// </text>
// <text id="F" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="43.4082031" y="273">F</tspan>
// </text>
// <text id="K" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="63.4355469" y="273">K</tspan>
// </text>
// <text id="1" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="85.1796875" y="273">1</tspan>
// </text>
// <text id="2" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="106.179688" y="273">2</tspan>
// </text>
// <text id="3" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="127.179688" y="273">3</tspan>
// </text>
// <text id="4" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="147.179688" y="273">4</tspan>
// </text>
// <text id="5" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="169.179688" y="273">5</tspan>
// </text>
// <text id="6" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="190.179688" y="273">6</tspan>
// </text>
// <text id="7" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="211.179688" y="273">7</tspan>
// </text>
// <text id="8" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="232.179688" y="273">8</tspan>
// </text>
// <text id="9" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="253.179688" y="273">9</tspan>
// </text>
// <text id="0" font-family="Roboto-Medium, Roboto" font-size="12" font-weight="400">
// <tspan x="274.179688" y="273">0</tspan>
// </text>

// </g>
