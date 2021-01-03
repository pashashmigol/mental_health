data class PersonBeingTested(val id: Long) {

    private var ongoingTest: MockTest? = null
    private var currentQuestion = 0

    fun startMockTest(): Question {
        ongoingTest = MockTest()
        return ongoingTest!!.questions[currentQuestion]
    }

    fun postAnswer(answer: Int): Question {
        currentQuestion++
        if(currentQuestion >= ongoingTest!!.questions.size){
            currentQuestion = 0
        }
        return ongoingTest!!.questions[currentQuestion]
    }
}

data class Question(
    val text: String,
    val options: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Question

        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        return text.hashCode()
    }
}