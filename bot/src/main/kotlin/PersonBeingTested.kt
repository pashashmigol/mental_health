import mmpi.MmpiTestingProcess
import telegram.TelegramMessage

data class PersonBeingTested(val id: Long) {
    companion object {
        const val TAG = "PersonBeingTested"
    }

    private var ongoingTest: MmpiTestingProcess? = null

    /**
     * Starts MmpiProcess test and returns first question
     * */
    fun startMmpiTestAndGetFirstQuestion(): TelegramMessage {
        println("$TAG: requestFirstQuestion();")
        ongoingTest = null
        return TelegramMessage.Question(createGenderQuestion())
    }

    fun notifyAnswerReceived(chosenOption: Int): TelegramMessage {
        if(ongoingTest == null) {//means we just get an answer on gender question
            val test = MmpiTestingProcess(Gender.byValue(chosenOption))
            ongoingTest = test
            return TelegramMessage.Question(ongoingTest!!.nextQuestion())
        }

        ongoingTest!!.submitAnswer(MmpiTestingProcess.Answer.byValue(chosenOption))
        return if (ongoingTest!!.hasNextQuestion()) {
            TelegramMessage.Question(ongoingTest!!.nextQuestion())
        } else {
            TelegramMessage.TestResult(ongoingTest!!.calculateResult())
        }
    }
}

private fun createGenderQuestion(): MmpiTestingProcess.Question = MmpiTestingProcess.Question(
    text = "Выберите себе пол:",
    options = listOf("Мужской", "Женский")
)
