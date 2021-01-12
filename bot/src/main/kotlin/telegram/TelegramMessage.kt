package telegram

import mmpi.Mmpi566

sealed class TelegramMessage

data class NextQuestion(val question: Mmpi566.Question) : TelegramMessage()
data class TestResult(val result: Mmpi566.Result) : TelegramMessage() {
    fun text(): String {
        val sb = StringBuilder()
        result.scalesToShow.forEach {
            sb.append("${it.name} : ${it.score} \n${it.description} \n\n")
        }
        return sb.toString()
    }
}