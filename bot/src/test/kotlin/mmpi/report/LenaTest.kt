package mmpi.report

import kotlinx.coroutines.runBlocking
import mmpi.*
import models.TypeOfTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.TestInstance

import models.User
import org.kodein.di.instance
import testDI

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LenaTest {

    private val mmpi377Data: MmpiData by testDI.instance(TypeOfTest.Mmpi377)

    @Test
    fun generateReport() = runBlocking {
        val answersList = agreeTo(
            5, 7, 10, 13, 14, 16, 18, 24, 32, 35, 44, 47, 57, 58, 65, 66, 67, 68, 69, 75, 79, 84, 87, 90, 92, 94, 95,
            96, 97, 98, 101, 110, 118, 120, 122, 123, 131, 138, 148, 149, 151, 152, 160, 163, 167, 170, 176, 181, 185,
            186, 187, 188, 193, 194, 195, 196, 198, 205, 210, 215, 216, 220, 223, 224, 228, 230, 233, 235, 240, 243,
            246, 249, 254, 256, 263, 269, 270, 272, 274, 282, 283, 284, 285, 293, 296, 298, 300, 301, 306, 312, 317,
            318, 319, 322, 323, 330, 340, 341, 347, 348, 349, 352, 358, 359, 360, 361, 362, 363, 370, 374, 377
        )
        val lena = User(
            id = 111,
            name = "LenaTest",
            googleDriveFolderUrl = "",
            googleDriveFolderId = "",
            runDailyQuiz = false
        )
        val result = calculateMmpi(
            answers = answersList,
            scales = mmpi377Data.scales(Gender.Female)
        )
        val report = generateHtml(
            user = lena,
            questions = mmpi377Data.questions(Gender.Female),
            answers = answersList,
            result = result
        )
        println(report)
    }
}