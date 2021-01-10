import mmpi.Mmpi566
import telegram.NextQuestion
import telegram.TelegramMessage
import telegram.TestResult

data class PersonToTest(val id: Long) {
    companion object {
        const val TAG = "Person"
    }
    private var ongoingTest: Mmpi566? = null

    fun requestFirstQuestion(): TelegramMessage {
        println("$TAG: requestFirstQuestion();")

        val test  = Mmpi566()
        ongoingTest = test

        val question = test.nextQuestion()
        return NextQuestion(question)
    }

    fun submitAnswer(chosenOption: Int): TelegramMessage {
        println("$TAG: submitAnswer();")

        val test = ongoingTest!!
        test.submitAnswer(Mmpi566.Answer.byValue(chosenOption))

        return if (test.hasNextQuestion()) {
            val question = test.nextQuestion()
            NextQuestion(question)
        } else {
            val result = test.calculateResult()
            TestResult(result = result)
        }
    }
}
