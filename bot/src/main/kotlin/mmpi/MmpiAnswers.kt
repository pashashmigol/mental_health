package mmpi

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.parseUtc
import models.Answers
import models.Type
import models.User

class MmpiAnswers(
    user: User,
    dateTime: DateTime,
    override val answers: List<MmpiProcess.Answer>
) : Answers(user, dateTime) {

    constructor(
        user: User,
        date: String,
        answers: List<MmpiProcess.Answer>,
    ) : this(user, DateFormat.FORMAT1.parseUtc(date), answers)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MmpiAnswers

        if (user != other.user) return false
        if (date != other.date) return false
        if (answers.size != other.answers.size) return false
        if (answers.zip(other.answers).any { it.first != it.second }) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + answers.hashCode()
        return result
    }
}