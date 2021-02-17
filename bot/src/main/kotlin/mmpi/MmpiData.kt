package mmpi

import Gender
import models.Question

class MmpiData(
    val questionsForMen: List<Question>,
    val questionsForWomen: List<Question>,
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
