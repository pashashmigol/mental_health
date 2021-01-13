package telegram

import mmpi.MmpiTestingProcess

sealed class TelegramMessage {
    data class Question(val question: MmpiTestingProcess.Question) : TelegramMessage()
    data class TestResult(val result: MmpiTestingProcess.Result) : TelegramMessage() {
        fun text(): String {
            val sb = StringBuilder()
            result.scalesToShow.forEach {
                sb.append("${it.name} : ${it.score} \n${it.description} \n\n")
            }
            return sb.toString()
        }
    }
}