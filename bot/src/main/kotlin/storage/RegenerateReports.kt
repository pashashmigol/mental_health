package storage

import Gender
import mmpi.MmpiAnswers
import mmpi.calculateMmpi
import models.TypeOfTest
import Result
import models.size
import java.lang.RuntimeException

suspend fun regenerateReports(userid: Long, gender: Gender): String {
    CentralDataStorage.apply {
        val user = usersStorage.getUser(userid)!!
        val result = usersStorage.getUserAnswers(user)

        result as Result.Success

        val mmpiAnswers = result.data.first() as MmpiAnswers
        val mmpiResult = calculateMmpi(
            answers = mmpiAnswers.answersList,
            scales = mmpi377Data.scales(Gender.Female)
        )

        CentralDataStorage.apply {
            val questions = when (Pair(gender, mmpiAnswers.answersList.size)) {
                Pair(Gender.Male, TypeOfTest.Mmpi377.size) -> mmpi377Data.questionsForMen
                Pair(Gender.Female, TypeOfTest.Mmpi377.size) -> mmpi377Data.questionsForWomen
                Pair(Gender.Male, TypeOfTest.Mmpi566.size) -> mmpi566Data.questionsForMen
                Pair(Gender.Female, TypeOfTest.Mmpi566.size) -> mmpi566Data.questionsForMen
                else -> throw RuntimeException()
            }

            val resultLink = saveMmpi(
                user = user,
                typeOfTest = TypeOfTest.Mmpi377,
                questions = questions,
                answers = mmpiAnswers,
                result = mmpiResult,
                saveAnswers = false
            )

            println("resultLink = $resultLink")

            return resultLink
        }
    }
}