package quiz

import storage.CentralDataStorage

sealed class DailyQuizAnswer(
    open val questionIndex: Int,
    open val questionText: String,
) {
    data class Option(
        override val questionIndex: Int,
        override val questionText: String,
        val option: DailyQuizOptions
    ) : DailyQuizAnswer(questionIndex, questionText)

    data class Text(
        override val questionIndex: Int,
        override val questionText: String,
        val text: String
    ) : DailyQuizAnswer(questionIndex, questionText)
}

enum class DailyQuizOptions {
    AWFUL, BAD, NORMAL, GOOD, EXCELLENT;

    val title: String
        get() = when (this) {
            AWFUL -> CentralDataStorage.string("awful")
            BAD -> CentralDataStorage.string("bad")
            NORMAL -> CentralDataStorage.string("normal")
            GOOD -> CentralDataStorage.string("good")
            EXCELLENT -> CentralDataStorage.string("excellent")
        }
}