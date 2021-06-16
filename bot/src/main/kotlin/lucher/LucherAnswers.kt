package lucher

import com.soywiz.klock.DateTimeTz
import models.Answers
import models.User

class LucherAnswers(
    user: User,
    date: DateTimeTz,
    val firstRound: List<LucherColor>,
    val secondRound: List<LucherColor>
) : Answers(user, date) {

//    constructor(
//        user: User,
//        date: String,
//        firstRound: List<LucherColor>,
//        secondRound: List<LucherColor>
//    ) : this(user, DateFormat.FORMAT1.parse(date), firstRound, secondRound)

//    override val data: Any = mapOf(
//        "firstRound" to firstRound,
//        "secondRound" to secondRound
//    )

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