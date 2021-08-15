package lucher

import telegram.Button
import telegram.UserAnswer

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
    LucherColor.Gray -> "https://drive.google.com/uc?export=download&id=16-HckEnkJK65V0cY_PQm_pBdyNdY8Oo3"
    LucherColor.Blue -> "https://drive.google.com/uc?export=download&id=1_x72xz9efKUH8mtr_z5PLPbUn7867W2z"
    LucherColor.Green -> "https://drive.google.com/uc?export=download&id=1PqwXhKftH8RqhW0uQQkaPjEVVTlAt3jr"
    LucherColor.Red -> "https://drive.google.com/uc?export=download&id=17mcTjWmMZ7HSTlprNJ3GcmMUgK4gGCWE"
    LucherColor.Yellow -> "https://drive.google.com/uc?export=download&id=1_vajYGSfRPjebx7vpK7J9bDI085kYduN"
    LucherColor.Violet -> "https://drive.google.com/uc?export=download&id=1RH9OIenFsmOT6U-xJpOSW5mGfdmzneeS"
    LucherColor.Brown -> "https://drive.google.com/uc?export=download&id=1Hoqz5gTC4eipiOtH3pk5-JOcB5jkLaTi"
    LucherColor.Black -> "https://drive.google.com/uc?export=download&id=1iXXodvyxt691jfo5TLl3f3XwiUs43QRp"
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
    Button(text = "$index - $name", userAnswer = UserAnswer.Lucher(this))

