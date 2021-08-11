package lucher.report

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import lucher.*
import storage.CentralDataStorage.string

//todo remove hardcode
fun generateReport(
    userId: String,
    answers: LucherAnswersContainer,
    result: LucherResult
): String = StringBuilder().appendHTML().apply {
    h1 { text(userId) }
    createAnswersTable(answers, result)
    result.stablePairs.entries
}.finalize().toString()

private fun TagConsumer<StringBuilder>.createAnswersTable(
    answers: LucherAnswersContainer,
    result: LucherResult
) {
    caption {
        b {
            style = "font-size:24px; text-align:center; margin:64px"
            text(string("answers"))
        }
    }
    body {
        showAnswers(answers)
        showResults(result)

        justHeader("Тревожность")
        justText("Первый раунд: ${result.firstRoundAnxiety}")
        justText("Второй раунд: ${result.secondRoundAnxiety}")
    }
}

private fun BODY.showAnswers(answers: LucherAnswersContainer) {
    table {
        tr {
            title("I")
            answersRow(answers.firstRound)
        }
        tr {
            title("II")
            answersRow(answers.secondRound)
        }
    }
}

private fun BODY.showResults(result: LucherResult) {
    descriptionForPairs("Стабильные пары", result.stablePairs)
    descriptionForPairs("Разбитые пары", result.brokenPairs)
    descriptionForPairs("Компенсирующие пары", result.contraversedPairs)
}

private fun BODY.descriptionForPairs(header: String, pairs: Map<LucherElement, String>) {
    if (pairs.isEmpty()) return

    justHeader(header)

    pairs.forEach { (element, string) ->

        val colors = when (element) {
            is LucherElement.Pair -> listOf(element.firstColor.color, element.secondColor.color)
            is LucherElement.Single -> listOf(element.color.color)
        }
        table {
            tr {
                answersRow(colors)
                pairDescription(string)
            }
        }
    }
}

private fun BODY.justHeader(header: String) {
    b {
        style = "font-size:32px; text-align:left; margin:64px"
        text(header)
    }
}

private fun BODY.justText(header: String) {
    div {
        style = "font-size:18px; text-align:left; margin:32px"
        text(header)
    }
}

private fun TR.pairDescription(string: String) {
    td {
        style = "font-size:16px; text-align:left; margin:16px"
        text(string)
    }
}

private fun TR.answersRow(answers: List<LucherColor>) = answers.forEach {
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

private fun TR.title(text: String) {
    th {
        b {
            style = "font-size:40px; text-align:center; margin:32px"
            text(text)
        }
    }
}

