@file:Suppress("UnnecessaryVariable")

package mmpi.report

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import mmpi.MmpiProcess
import models.Question
import mmpi.reports.chart

fun generateReport(
    userId: String,
    questions: List<Question>,
    answers: List<MmpiProcess.Answer>,
    result: MmpiProcess.Result
): String {
    val answerItems = questions.zip(answers)

    val html = StringBuilder().appendHTML().apply {
        h1 { text(userId) }

        body {
            chart(chartFor(result))

            dl {
                result.scalesToShow.forEach { scale ->
                    val score = if (scale.useRawValuesForDescription) {
                        scale.score
                    } else {
                        scale.raw
                    }
                    dt {
                        b { text("${scale.name}: $score") }
                    }
                    dd { text("\n" + scale.description) }
                }
            }
            table {
                answerItems.forEach {
                    tr {
                        style = "border: 1px solid black;"
                        td {
                            style = "border: 1px solid black;"
                            text(it.first.text)
                        }
                        th {
                            style = "border: 1px solid black;"
                            text(it.second.text)
                        }
                    }
                }
            }
        }
    }.finalize().toString()
    return html
}
