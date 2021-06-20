package models

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz

abstract class Answers(
    val user: User,
    val date: DateTimeTz
) {
    val dateString: String = date.toString(DateFormat.DEFAULT_FORMAT)

    override fun toString(): String {
        return "Answers(user=$user, date=$date, dateString='$dateString')"
    }
}