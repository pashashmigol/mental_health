package mmpi

import Gender
import models.Question
import storage.CentralDataStorage

const val NUMBER_OF_QUESTIONS = 566

class MmpiProcess(gender: Gender) {
    internal data class State(
        val currentQuestionIndex: Int,// = 0,
        val questions: List<Question>,// = CurrentQuestionsProvider.MmpiProcessQuestions,
        val answers: List<Answer>,// = arrayOfNulls<Answer>(NUMBER_OF_QUESTIONS)
        val scales: Scales?
    )

    private var state = State(
        currentQuestionIndex = 0,
        questions = CentralDataStorage.mmpiData.questions(gender),
        answers = emptyList(),
        scales = CentralDataStorage.mmpiData.scales(gender)
    )

    val answers
        get() = state.answers
    val questions
        get() = state.questions

    fun submitAnswer(answer: Answer) {
        state = submitAnswer(state, answer)
    }

    fun hasNextQuestion(): Boolean = hasNextQuestion(state)

    fun nextQuestion(): Question {
        val (newState, question) = nextQuestion(state)
        state = newState
        return question
    }

    fun calculateResult() = calculateResult(state)

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

        val text
            get() = when (this) {
                Agree -> "Согласен"
                PartiallyAgree -> "Частично согласен"
                NotSure -> "Не уверен"
                PartiallyDisagree -> "Немного согласен"
                Disagree -> "Вообще не согласен"
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

        fun format(): String {
            val sb = StringBuilder()
            scalesToShow.forEach {
                sb.append("${it.name} : ${it.score} \n${it.description} \n\n")
            }
            return sb.toString()
        }
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

private fun submitAnswer(
    state: MmpiProcess.State,
    answer: MmpiProcess.Answer
): MmpiProcess.State {
    val newIndex = state.currentQuestionIndex + 1
    val answers = state.answers + answer
    return state.copy(currentQuestionIndex = newIndex, answers = answers)
}

private fun hasNextQuestion(state: MmpiProcess.State): Boolean {
    return state.currentQuestionIndex < NUMBER_OF_QUESTIONS
}

private fun nextQuestion(state: MmpiProcess.State):
        Pair<MmpiProcess.State, Question> {
    val question = state.questions[state.currentQuestionIndex]
    return Pair(state, question)
}

private fun calculateResult(state: MmpiProcess.State): MmpiProcess.Result {
    if (state.answers.size != NUMBER_OF_QUESTIONS)
        throw RuntimeException("Not all questions are answered")
    if (state.scales == null)
        throw RuntimeException("Scales are not loaded")

    return calculate(state.answers, state.scales)
}
