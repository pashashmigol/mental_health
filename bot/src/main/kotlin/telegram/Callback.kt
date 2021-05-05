package telegram

import Gender
import lucher.LucherColor
import mmpi.MmpiProcess
import models.TypeOfTest
import quiz.DailyQuizAnswer

sealed class Callback(val type: Type) {

    enum class Type {
        Gender, Mmpi, Lucher, NewTestRequest, DailyQuiz
    }

    abstract fun makeString(): String

    companion object {
        fun fromString(data: String): Callback {

            val (typeStr, valueStr, indexStr) = data.split(":")

            return when (Type.valueOf(typeStr)) {
                Type.NewTestRequest -> NewTest(TypeOfTest.valueOf(valueStr))
                Type.Gender -> GenderAnswer(Gender.valueOf(valueStr))
                Type.Mmpi -> Mmpi(index = indexStr.toInt(), MmpiProcess.Answer.valueOf(valueStr))
                Type.Lucher -> Lucher(LucherColor.valueOf(valueStr))
                Type.DailyQuiz -> DailyQuiz(DailyQuizAnswer.valueOf(valueStr))
            }
        }
    }

    class GenderAnswer(val answer: Gender) : Callback(Type.Gender) {
        override fun makeString() = "${Type.Gender}:$answer:"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GenderAnswer
            if (answer != other.answer) return false
            return true
        }

        override fun hashCode(): Int {
            return answer.hashCode()
        }

        override fun toString(): String {
            return "GenderAnswer(answer=$answer)"
        }
    }

    class Mmpi(val index: Int, val answer: MmpiProcess.Answer) : Callback(Type.Mmpi) {
        override fun makeString() = "${Type.Mmpi}:$answer:$index"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Mmpi
            if (answer != other.answer) return false
            return true
        }

        override fun hashCode(): Int {
            return answer.hashCode()
        }

        override fun toString(): String {
            return "MmpiAnswer(answer=$answer)"
        }
    }

    class Lucher(val answer: LucherColor) : Callback(Type.Lucher) {
        override fun makeString() = "${Type.Lucher}:$answer:"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Lucher
            if (answer != other.answer) return false
            return true
        }

        override fun hashCode(): Int {
            return answer.hashCode()
        }

        override fun toString(): String {
            return "LucherAnswer(answer=$answer)"
        }
    }

    class NewTest(val typeOfTest: TypeOfTest) : Callback(Type.NewTestRequest) {
        override fun makeString() = "${Type.NewTestRequest}:$typeOfTest:"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NewTest
            if (typeOfTest != other.typeOfTest) return false
            return true
        }

        override fun hashCode(): Int {
            return typeOfTest.hashCode()
        }

        override fun toString(): String {
            return "NewTestRequest(typeOfTest=$typeOfTest)"
        }
    }

    class DailyQuiz(val answer: DailyQuizAnswer) : Callback(Type.DailyQuiz) {
        override fun makeString() = "${Type.DailyQuiz}:$answer:"
    }
}