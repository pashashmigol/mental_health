package mmpi

class Mmpi566 {
    companion object {
        const val TAG = "mmpi.MmpiTest"
    }

    private var _questions = CurrentQuestionsProvider.mmpi566Questions
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
        if (_answers.filterNotNull().size != 566)
            throw RuntimeException("Not all questions are answered")

        println("$TAG: calculateResult()")
        return calculate(_answers, CurrentQuestionsProvider.mmpi566Scales!!)
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

    class Result(
        val description: String,
        liesScale: Scale.Result,
        credibilityScale: Scale.Result,
        introversionScale: Scale.Result,
        overControlScale1: Scale.Result,
        passivityScale2: Scale.Result,
        labilityScale3: Scale.Result,
        impulsivenessScale4: Scale.Result,
        masculinityScale5: Scale.Result,
        rigidityScale6: Scale.Result,
        anxietyScale7: Scale.Result,
        individualismScale8: Scale.Result,
        optimismScale9: Scale.Result
    ) {

        val scalesToShow = listOf(
            liesScale,
            credibilityScale,
            introversionScale,
            overControlScale1,
            passivityScale2,
            labilityScale3,
            impulsivenessScale4,
            masculinityScale5,
            rigidityScale6,
            anxietyScale7,
            individualismScale8,
            optimismScale9
        )
    }

    data class Scales(
        val correctionScale: Scale,
        val liesScale: Scale,
        val credibilityScale: Scale,
        val introversionScale: Scale,
        val overControlScale1: Scale,
        val passivityScale2: Scale,
        val labilityScale3: Scale,
        val impulsivenessScale4: Scale,
        val masculinityScale5: Scale,
        val rigidityScale6: Scale,
        val anxietyScale7: Scale,
        val individualismScale8: Scale,
        val optimismScale9: Scale
    )
}
