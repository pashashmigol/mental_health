package quiz

import com.soywiz.klock.DateTimeTz
import models.Answers
import models.User
import storage.CentralDataStorage.string

class DailyQuizAnswers(
    user: User,
    date: DateTimeTz,
    val answers: List<Answer>
): Answers(user, date)  {
    data class Answer(
        val questionIndex: Int,
        val questionText: String,
        val option: Option
    )

    enum class Option {
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DailyQuizAnswers

        if (answers != other.answers) return false

        return true
    }

    override fun hashCode(): Int {
        return answers.hashCode()
    }

    override fun toString(): String {
        return "DailyQuizAnswers(answers=$answers)"
    }
}