package lucher

import com.soywiz.klock.DateTimeTz
import models.AnswersContainer
import models.User

class LucherAnswersContainer(
    user: User,
    date: DateTimeTz,
    val firstRound: List<LucherColor>,
    val secondRound: List<LucherColor>
) : AnswersContainer(user, date) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LucherAnswersContainer

        if (firstRound != other.firstRound) return false
        if (secondRound != other.secondRound) return false

        return true
    }

    override fun hashCode(): Int {
        var result = firstRound.hashCode()
        result = 31 * result + secondRound.hashCode()
        return result
    }
}