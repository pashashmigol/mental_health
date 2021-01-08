import mmpi.Mmpi566

data class Person(val id: Long) {
    companion object {
        const val TAG = "Person"
    }
    private var ongoingTest: Mmpi566? = null

    fun requestFirstQuestion(): Response {
        println("$TAG: requestFirstQuestion();")

        val test  = Mmpi566()
        ongoingTest = test

        val question = test.nextQuestion()
        return Response.NextQuestion(question)
    }

    fun submitAnswer(chosenOption: Int): Response {
        println("$TAG: submitAnswer();")

        val test = ongoingTest!!
        test.submitAnswer(Mmpi566.Answer.byValue(chosenOption))

        return if (test.hasNextQuestion()) {
            val question = test.nextQuestion()
            Response.NextQuestion(question)
        } else {
            val result = test.calculateResult()
            Response.TestResult(description = result.description)
        }
    }

    sealed class Response {
        data class NextQuestion(val question: Mmpi566.Question) : Response()
        data class TestResult(val description: String) : Response()
    }
}