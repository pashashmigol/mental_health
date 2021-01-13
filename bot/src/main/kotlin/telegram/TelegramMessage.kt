package telegram

import mmpi.MmpiProcess

sealed class TelegramMessage

data class NextQuestion(val question: MmpiProcess.Question) : TelegramMessage()
data class TestResult(val result: MmpiProcess.Result) : TelegramMessage() {
    fun text(): String {
        val sb = StringBuilder()
        result.scalesToShow.forEach {
            sb.append("${it.name} : ${it.score} \n${it.description} \n\n")
        }
        return sb.toString()
    }
}