package mmpi

import Gender
import models.*
import storage.CentralDataStorage
import storage.CentralDataStorage.string

class MmpiProcess(gender: Gender, val typeOfTest: TypeOfTest) {

    internal data class State(
        val currentQuestionIndex: Int = -1,
        val questions: List<Question>,
        val answers: List<Answer>,
        val scales: Scales?
    )

    private var state = when (typeOfTest) {
        TypeOfTest.Mmpi566 -> State(
            questions = CentralDataStorage.mmpi566Data.questions(gender),
            answers = emptyList(),
            scales = CentralDataStorage.mmpi566Data.scales(gender)
        )
        TypeOfTest.Mmpi377 -> State(
            questions = CentralDataStorage.mmpi377Data.questions(gender),
            answers = emptyList(),
            scales = CentralDataStorage.mmpi377Data.scales(gender)
        )
        else -> throw IllegalStateException()
    }

    val answers
        get() = state.answers
    val questions
        get() = state.questions


    fun isItLastAskedQuestion(index: Int): Boolean {
        return index == state.currentQuestionIndex
    }

    fun allQuestionsAreAnswered(): Boolean {
        return state.answers.size == state.questions.size
    }

    fun submitAnswer(index: Int, answer: Answer) {
        if (index < questions.size)
            state = submitAnswer(state, index, answer)
    }

    fun hasNextQuestion(): Boolean = hasNextQuestion(state, typeOfTest)

    fun nextQuestion(): Question {
        val (newState, question) = nextQuestion(state)
        state = newState
        return question
    }

    fun calculateResult() = calculateResult(state, typeOfTest)

    enum class Answer {
        Agree,
        Disagree;
        val text
            get() = when (this) {
                Agree -> string("agree")
                Disagree -> string("disagree")
            }
    }

    class Result(
        val liesScaleL: Scale.Result,
        val credibilityScaleF: Scale.Result,
        val correctionScaleK: Scale.Result,
        val introversionScale0: Scale.Result,
        val overControlScale1: Scale.Result,
        val passivityScale2: Scale.Result,
        val labilityScale3: Scale.Result,
        val impulsivenessScale4: Scale.Result,
        val masculinityScale5: Scale.Result,
        val rigidityScale6: Scale.Result,
        val anxietyScale7: Scale.Result,
        val individualismScale8: Scale.Result,
        val optimismScale9: Scale.Result
    ) {
        val scalesToShow = listOf(
            liesScaleL,
            credibilityScaleF,
            correctionScaleK,
            overControlScale1,
            passivityScale2,
            labilityScale3,
            impulsivenessScale4,
            masculinityScale5,
            rigidityScale6,
            anxietyScale7,
            individualismScale8,
            optimismScale9,
            introversionScale0
        )
    }

    data class Scales(
        val correctionScaleK: Scale,
        val liesScaleL: Scale,
        val credibilityScaleF: Scale,
        val introversionScale0: Scale,
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
    index: Int,
    answer: MmpiProcess.Answer
): MmpiProcess.State {
    val answers = state.answers.toMutableList()

    if (answers.lastIndex < index) {
        answers.add(index, answer)
    } else {
        answers[index] = answer
    }

    return state.copy(
        answers = answers.toList()
    )
}

private fun hasNextQuestion(state: MmpiProcess.State, typeOfTest: TypeOfTest): Boolean {
    return state.currentQuestionIndex < typeOfTest.size - 1
}

private fun nextQuestion(state: MmpiProcess.State):
        Pair<MmpiProcess.State, Question> {

    val newState = state.copy(currentQuestionIndex = state.currentQuestionIndex + 1)
    val question = state.questions[newState.currentQuestionIndex]

    return Pair(newState, question)
}

private fun calculateResult(state: MmpiProcess.State, typeOfTest: TypeOfTest): MmpiProcess.Result {
    if (state.answers.size != typeOfTest.size)
        throw RuntimeException("Not all questions are answered")
    if (state.scales == null)
        throw RuntimeException("Scales are not loaded")

    return calculateMmpi(state.answers, state.scales)
}
