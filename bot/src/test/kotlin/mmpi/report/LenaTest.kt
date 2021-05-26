package mmpi.report

import mmpi.agreeTo
import mmpi.calculate
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import storage.CentralDataStorage

import models.User
import telegram.LaunchMode

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LenaTest {

    @BeforeAll
    fun setup() {
        CentralDataStorage.init(
            rootPath = LaunchMode.TESTS.rootPath,
            testingMode = true
        )
    }

    @Test
    fun generateReport() {
        val answers = agreeTo(
            5, 7, 10, 13, 14, 16, 18, 24, 32, 35, 44, 47, 57, 58, 65, 66, 67, 68, 69, 75, 79, 84, 87, 90, 92, 94, 95,
            96, 97, 98, 101, 110, 118, 120, 122, 123, 131, 138, 148, 149, 151, 152, 160, 163, 167, 170, 176, 181, 185,
            186, 187, 188, 193, 194, 195, 196, 198, 205, 210, 215, 216, 220, 223, 224, 228, 230, 233, 235, 240, 243,
            246, 249, 254, 256, 263, 269, 270, 272, 274, 282, 283, 284, 285, 293, 296, 298, 300, 301, 306, 312, 317,
            318, 319, 322, 323, 330, 340, 341, 347, 348, 349, 352, 358, 359, 360, 361, 362, 363, 370, 374, 377
        )
        val result = calculate(
            answers = answers,
            scales = CentralDataStorage.mmpi377Data.scales(Gender.Female)
        )
        val report = generateReport(
            user = User(id = 0, name = "Елена Жебрикова", googleDriveFolder = ""),
            questions = CentralDataStorage.mmpi377Data.questions(Gender.Female),
            answers = answers,
            result = result
        )
        println(report)
    }
}