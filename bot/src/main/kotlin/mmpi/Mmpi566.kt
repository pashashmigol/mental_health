package mmpi

import CurrentQuestionsProvider

class Mmpi566 {
    companion object {
        const val TAG = "mmpi.MmpiTest"
    }

    private var _questions = CurrentQuestionsProvider.mock566Questions
    private val _answers = arrayOfNulls<Answer>(566)

    init {
        assert(_questions.size == 566)
    }

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
        if(_answers.filterNotNull().size != 566)
            throw RuntimeException("Not all questions are answered")

        println("$TAG: calculateResult()")
        return notCompletedTest(_answers)
    }

    data class Question(
        val text: String,
        val options: List<String>
    )

    enum class Answer(val option: Int) {
        Agree(0),
        PartiallyAgree(1),
        NotSure(2),
        PartiallyDisagree(3),
        Disagree(4);

        companion object {
            private val VALUES = values()
            fun byValue(value: Int) = VALUES.firstOrNull { it.option == value } ?: NotSure
        }

    }

    class Result(val description: String,
                 val introversionScale: Scale.Result,
                 val overControlScale1: Scale.Result,
                 val passivityScale2: Scale.Result,
                 val labilityScale3: Scale.Result,
                 val impulsivenessScale4: Scale.Result,
                 val masculinityScale5: Scale.Result,
                 val rigidityScale6: Scale.Result,
                 val anxietyScale7: Scale.Result,
                 val individualismScale8: Scale.Result,
                 val optimismScale9: Scale.Result)
}
