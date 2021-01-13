import mmpi.MmpiProcess
import telegram.NextQuestion
import telegram.TelegramMessage
import telegram.TestResult

data class PersonBeingTested(val id: Long) {
    companion object {
        const val TAG = "PersonBeingTested"
    }
    private var ongoingTest: MmpiProcess? = null

    /**
     * Starts MmpiProcess test and returns first question
     * */
    fun startMmpiProcessTest(): TelegramMessage {
        println("$TAG: requestFirstQuestion();")

        val test  = MmpiProcess()
        ongoingTest = test

        return NextQuestion(test.firstQuestion())
    }

    fun notifyAnswerReceived(chosenOption: Int): TelegramMessage {
        println("$TAG: submitAnswer();")

        ongoingTest!!.submitAnswer(MmpiProcess.Answer.byValue(chosenOption))

        return if (ongoingTest!!.hasNextQuestion()) {
            NextQuestion(ongoingTest!!.nextQuestion())
        } else {
            TestResult(ongoingTest!!.calculateResult())
        }
    }
}
