package lucher

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.parseUtc
import mmpi.MmpiProcess
import models.Answers
import models.Type
import models.User

class LucherAnswers(
    user: User,
    dateTime: DateTime,
    val firstRound: List<LucherColor>,
    val secondRound: List<LucherColor>
) : Answers(user, dateTime) {

    constructor(
        user: User,
        date: String,
        firstRound: List<LucherColor>,
        secondRound: List<LucherColor>
    ) : this(user, DateFormat.FORMAT1.parseUtc(date), firstRound, secondRound)

    override val data: Any = mapOf(
        "firstRound" to firstRound,
        "secondRound" to secondRound
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LucherAnswers

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