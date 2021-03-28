package models

enum class Type(private val index: Int) {
    Mmpi566(0), Mmpi377(1), Lucher(2);

    companion object {
        fun of(index: String): Type {
            return values().first {
                it.index == index.toInt()
            }
        }
    }
}

val Type.size: Int
    get() = when (this) {
        Type.Mmpi566 -> 566
        Type.Mmpi377 -> 377
        Type.Lucher -> 7
    }