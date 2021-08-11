package quiz

import com.soywiz.klock.DateTimeTz
import models.AnswersContainer
import models.User

class DailyQuizAnswersContainer(
    user: User,
    date: DateTimeTz,
    val answers: List<DailyQuizAnswer>
): AnswersContainer(user, date)  {

    override fun toString(): String {
        return "AnswersContainer(answers=$answers)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DailyQuizAnswersContainer

        if (answers != other.answers) return false

        return true
    }

    override fun hashCode(): Int {
        return answers.hashCode()
    }
}