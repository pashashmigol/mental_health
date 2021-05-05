package quiz

import storage.CentralDataStorage.string

enum class DailyQuizAnswer {
    AWFUL, BAD, NORMAL, GOOD, EXCELLENT;

    val title: String
        get() = when (this) {
            AWFUL -> string("awful")
            BAD -> string("bad")
            NORMAL -> string("normal")
            GOOD -> string("good")
            EXCELLENT -> string("excellent")
        }
}