package telegram

import Gender
import lucher.LucherColor
import mmpi.MmpiProcess
import models.TypeOfTest


sealed class Callback(val type: Type) {

    enum class Type {
        Gender, Mmpi, Lucher, NewTestRequest
    }

    abstract fun makeString(): String


    companion object {
        fun fromString(data: String): Callback {

            val (typeStr, valueStr, indexStr) = data.split(":")

            return when (Type.valueOf(typeStr)) {
                Type.NewTestRequest -> NewTestRequest(TypeOfTest.valueOf(valueStr))
                Type.Gender -> GenderAnswer(Gender.valueOf(valueStr))
                Type.Mmpi -> MmpiAnswer(index = indexStr.toInt(), MmpiProcess.Answer.valueOf(valueStr))
                Type.Lucher -> LucherAnswer(LucherColor.valueOf(valueStr))
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

    class MmpiAnswer(val index: Int, val answer: MmpiProcess.Answer) : Callback(Type.Mmpi) {
        override fun makeString() = "${Type.Mmpi}:$answer:$index"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MmpiAnswer
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

    class LucherAnswer(val answer: LucherColor) : Callback(Type.Lucher) {
        override fun makeString() = "${Type.Lucher}:$answer:"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LucherAnswer
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


    class NewTestRequest(val typeOfTest: TypeOfTest) : Callback(Type.NewTestRequest) {
        override fun makeString() = "${Type.NewTestRequest}:$typeOfTest:"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NewTestRequest
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
}