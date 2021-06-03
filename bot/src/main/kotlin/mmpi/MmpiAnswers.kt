package mmpi

import Gender
import com.soywiz.klock.DateTimeTz
import models.Answers
import models.User

class MmpiAnswers(
    user: User,
    date: DateTimeTz,
    val gender: Gender,
    val answersList: List<MmpiProcess.Answer>
) : Answers(user, date) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MmpiAnswers

        if (user != other.user) return false
        if (date != other.date) return false
        if (answersList.size != other.answersList.size) return false
        if (answersList.zip(other.answersList).any { it.first != it.second }) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + answersList.hashCode()
        return result
    }
}