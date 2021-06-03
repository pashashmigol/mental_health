package models

enum class TestType {
    Mmpi566, Mmpi377, Lucher;
}

val TestType.size: Int
    get() = when (this) {
        TestType.Mmpi566 -> 566
        TestType.Mmpi377 -> 377
        TestType.Lucher -> 7
    }