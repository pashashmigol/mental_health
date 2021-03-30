package models

enum class Type {
    Mmpi566, Mmpi377, Lucher;
}

val Type.size: Int
    get() = when (this) {
        Type.Mmpi566 -> 566
        Type.Mmpi377 -> 377
        Type.Lucher -> 7
    }