package lucher.report

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import lucher.LucherAnswers
import lucher.LucherColor
import lucher.LucherResult
import lucher.url

fun generateReport(
    userId: String,
    answers: LucherAnswers,
    result: LucherResult
): String = StringBuilder().appendHTML().apply {
    h1 { text(userId) }
    createAnswersTable(answers)
    result.stablePairs.entries
}.finalize().toString()

private fun TagConsumer<StringBuilder>.createAnswersTable(answers: LucherAnswers) {
    caption {
        b {
            style = "font-size:24px; text-align:center; margin:64px"
            text("Ответы")
        }
    }
    body {
        table {
            tr {
                th {
                    b {
                        style = "font-size:40px; text-align:center; margin:32px"
                        text("I")
                    }
                }
                createAnswersRow(answers.firstRound)
            }
            tr {
                th {
                    b {
                        style = "font-size:40px; text-align:center; margin:32px"
                        text("II")
                    }
                }
                createAnswersRow(answers.secondRound)
            }
        }
    }
}

private fun TR.createAnswersRow(answers: List<LucherColor>) = answers.forEach {
        td {
            div {
                style = "width:50px; height:50px; position:relative"
                div {
                    img {
                        style =
                            "width:100%; height:100%; position:absolute; top:0; left:0;z-index:-1"
                        src = it.url()
                    }
                }
                div {
                    style = "color:#ffffff; font-size:40px; text-align:center"
                    b { text("${it.index + 1}") }
                }
            }
        }
    }

