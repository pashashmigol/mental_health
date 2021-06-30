package lucher

import telegram.Button
import telegram.Callback

enum class LucherColor(val index: Int) {
    Gray(index = 0),
    Blue(index = 1),
    Green(index = 2),
    Red(index = 3),
    Yellow(index = 4),
    Violet(index = 5),
    Brown(index = 6),
    Black(index = 7);

    companion object {
        fun of(index: String): LucherColor {
            return values().first { it.index == index.toInt() }
        }
    }
}

fun LucherColor.url(): String = when (this) {
    LucherColor.Gray -> "https://drive.google.com/uc?export=download&id=1GuQ5B2jFD48rRVZcWjgB2VYymzZh9my1"
    LucherColor.Blue -> "https://drive.google.com/uc?export=download&id=1FbQHlO_eycM9SVUOPkXJhEyjUzOKxEHF"
    LucherColor.Green -> "https://drive.google.com/uc?export=download&id=12pgfDxHfe3BMZwJelx0oA8PaHrrGEL5k"
    LucherColor.Red -> "https://drive.google.com/uc?export=download&id=16HI01RELVjYcOyW9WBH46yP435-XshJu"
    LucherColor.Yellow -> "https://drive.google.com/uc?export=download&id=1fmoDra7KpOukr8Pveu2RabQxE618AfwC"
    LucherColor.Violet -> "https://drive.google.com/uc?export=download&id=1RJKBMtE7A1-serZ3yT-wFFmfKieSbURw"
    LucherColor.Brown -> "https://drive.google.com/uc?export=download&id=1QsRaeZ9KVI0GSQCF2AIGrXmUlL9sc1P0"
    LucherColor.Black -> "https://drive.google.com/uc?export=download&id=1DHISDgiM6HPFWrDOnC09L1K9WGmTnpmX"
}

fun LucherColor.toARGB(): Int = when (this) {
    LucherColor.Gray -> 0xff94989B.toInt()
    LucherColor.Blue -> 0xff014983.toInt()
    LucherColor.Green -> 0xff007477.toInt()
    LucherColor.Red -> 0xffEB1C30.toInt()
    LucherColor.Yellow -> 0xffFEE600.toInt()
    LucherColor.Violet -> 0xffD80081.toInt()
    LucherColor.Brown -> 0xff9D685A.toInt()
    LucherColor.Black -> 0xff231F1F.toInt()
}

fun LucherColor.callbackData() =
    Button(text = "$index - $name", callback = Callback.LucherAnswer(this))

