class MmpiTest {
    companion object {
        const val TAG = "MmpiTest"
    }

    private var _questions = CurrentQuestionsProvider.mockTestQuestions
    private val _answers = arrayOfNulls<Answer>(_questions.size)

    private var _currentQuestionIndex = 0

    fun submitAnswer(answer: Answer) {
        println("$TAG: submitAnswer();")
        _answers[_currentQuestionIndex++] = answer
    }

    fun hasNextQuestion(): Boolean {
        val res = _answers.size > _currentQuestionIndex
        println("$TAG: hasNextQuestion(); $res")
        return res
    }

    fun nextQuestion(): Question {
        println("$TAG: nextQuestion()")
        return _questions[_currentQuestionIndex]
    }

    fun calculateResult(): Result {
        println("$TAG: calculateResult()")
        val score = _answers.sumOf { it?.option ?: 0 }
        return Result("You've got $score. It seems you have an issue")
    }

    class Result(val description: String)
    class Answer(val option: Int)

    data class Question(
        val text: String,
        val options: List<String>
    )
}
