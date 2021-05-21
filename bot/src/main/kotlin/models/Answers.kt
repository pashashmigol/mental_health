package models

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime

abstract class Answers(
    val user: User,
    val date: DateTime
) {
    abstract val data: Any

    val dateString: String = date.toString(DateFormat.FORMAT1)

    override fun toString(): String {
        return "Answers(user=$user, date=$date, dateString='$dateString')"
    }
}