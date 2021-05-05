package models

enum class TypeOfTest {
    Mmpi566, Mmpi377, Lucher, DailyQuiz;
}

val TypeOfTest.size: Int
    get() = when (this) {
        TypeOfTest.Mmpi566 -> 566
        TypeOfTest.Mmpi377 -> 377
        TypeOfTest.Lucher -> 7
        TypeOfTest.DailyQuiz -> 2
    }