package mmpi

import CurrentQuestionsProvider

class MmpiTest {
    companion object {
        const val TAG = "mmpi.MmpiTest"
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
        return calculateMmpi(_answers)
    }

    class Result(val description: String)

    enum class Answer(val option: Int) {
        Agree(0),
        PartiallyAgree(1),
        NotSure(2),
        PartiallyDisagree(3),
        Disagree(4)
    }

    data class Question(
        val text: String,
        val options: List<String>
    )
}
