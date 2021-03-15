package mmpi

import Gender
import models.Question
import storage.CentralDataStorage
import storage.CentralDataStorage.string

const val NUMBER_OF_QUESTIONS = 566

class MmpiProcess(gender: Gender, type: Type) {

    internal data class State(
        val currentQuestionIndex: Int,// = 0,
        val questions: List<Question>,// = CurrentQuestionsProvider.MmpiProcessQuestions,
        val answers: List<Answer>,// = arrayOfNulls<Answer>(NUMBER_OF_QUESTIONS)
        val scales: Scales?
    )

    private var state = when (type) {
        Type.Mmpi566 -> State(
            currentQuestionIndex = 0,
            questions = CentralDataStorage.mmpi566Data.questions(gender),
            answers = emptyList(),
            scales = CentralDataStorage.mmpi566Data.scales(gender)
        )
        Type.Mmpi377 -> State(
            currentQuestionIndex = 0,
            questions = CentralDataStorage.mmpi377Data.questions(gender),
            answers = emptyList(),
            scales = CentralDataStorage.mmpi377Data.scales(gender)
        )
    }

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
        Disagree(4);

        companion object {
            private val VALUES = values()
            fun byValue(value: Int) = VALUES.firstOrNull { it.option == value } ?: Disagree
        }

        val text
            get() = when (this) {
                Agree -> string("agree")
                Disagree -> string("disagree")
            }
    }

    class Result(
        val description: String,
        liesScale: Scale.Result,
        credibilityScale: Scale.Result,
        correctionScale: Scale.Result,
        introversionScale0: Scale.Result,
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
            correctionScale,
            introversionScale0,
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
    ) {
        override fun toString(): String {
            return "Scales(masculinityScale5=$masculinityScale5)"
        }
    }
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
