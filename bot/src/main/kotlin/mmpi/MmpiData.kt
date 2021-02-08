package mmpi

import Gender

class MmpiData(
    val questionsForMen: List<Message.Question>,
    val questionsForWomen: List<Message.Question>,
    val scalesForMen: MmpiTestingProcess.Scales,
    val scalesForWomen: MmpiTestingProcess.Scales,
) {
    fun questions(gender: Gender) = when (gender) {
        Gender.Male -> questionsForMen
        Gender.Female -> questionsForWomen
    }
    fun scales(gender: Gender) = when (gender) {
        Gender.Male -> scalesForMen
        Gender.Female -> scalesForWomen
    }
}
