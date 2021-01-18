import mmpi.MmpiTestingProcess

//data class PersonBeingTested(val id: Long) {
//    companion object {
//        const val TAG = "PersonBeingTested"
//    }
//
//    private var ongoingTest: MmpiTestingProcess? = null
//
//    /**
//     * Starts MmpiProcess test and returns first question
//     * */
//    fun startMmpiTestAndGetFirstQuestion(): Message {
//        println("$TAG: requestFirstQuestion();")
//        ongoingTest = null
//        return createGenderQuestion()
//    }
//
//    fun notifyAnswerReceived(chosenOption: Int): Message {
//        if(ongoingTest == null) {//means we just get an answer on gender question
//            val test = MmpiTestingProcess(Gender.byValue(chosenOption))
//            ongoingTest = test
//            return ongoingTest!!.nextQuestion()
//        }
//
//        ongoingTest!!.submitAnswer(MmpiTestingProcess.Answer.byValue(chosenOption))
//        return if (ongoingTest!!.hasNextQuestion()) {
//            ongoingTest!!.nextQuestion()
//        } else {
//            Message.TestResult(ongoingTest!!.calculateResult().format())
//        }
//    }
//}

private fun createGenderQuestion() = Message.Question(
    text = "Выберите себе пол:",
    options = listOf("Мужской", "Женский")
)
