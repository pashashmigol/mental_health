package lucher

import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

enum class LucherColor(val index: Int, val url: String) {
    Gray(index = 0, url = "https://drive.google.com/uc?export=download&id=1GuQ5B2jFD48rRVZcWjgB2VYymzZh9my1"),
    Blue(index = 1, url = "https://drive.google.com/uc?export=download&id=1FbQHlO_eycM9SVUOPkXJhEyjUzOKxEHF"),
    Green(index = 2, url = "https://drive.google.com/uc?export=download&id=12pgfDxHfe3BMZwJelx0oA8PaHrrGEL5k"),
    Rtd(index = 3, url = "https://drive.google.com/uc?export=download&id=16HI01RELVjYcOyW9WBH46yP435-XshJu"),
    Yellow(index = 4, url = "https://drive.google.com/uc?export=download&id=1fmoDra7KpOukr8Pveu2RabQxE618AfwC"),
    Violet(index = 5, url = "https://drive.google.com/uc?export=download&id=1RJKBMtE7A1-serZ3yT-wFFmfKieSbURw"),
    Brown(index = 6, url = "https://drive.google.com/uc?export=download&id=1QsRaeZ9KVI0GSQCF2AIGrXmUlL9sc1P0"),
    Black(index = 7, url = "https://drive.google.com/uc?export=download&id=1DHISDgiM6HPFWrDOnC09L1K9WGmTnpmX");

    companion object {
        fun valueOf(index: Int): LucherColor {
            return Black
        }
    }
}

fun LucherColor.callbackData() =
    InlineKeyboardButton.CallbackData(text = index.toString(), callbackData = index.toString())

