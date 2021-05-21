package mmpi

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.parseUtc
import models.Answers
import models.User

class MmpiAnswers(
    user: User,
    dateTime: DateTime,
    override val data: List<MmpiProcess.Answer>
) : Answers(user, dateTime) {

    constructor(
        user: User,
        dateString: String,
        answers: List<MmpiProcess.Answer>,
    ) : this(user, DateFormat.FORMAT1.parseUtc(dateString), answers)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MmpiAnswers

        if (user != other.user) return false
        if (date != other.date) return false
        if (data.size != other.data.size) return false
        if (data.zip(other.data).any { it.first != it.second }) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }
}