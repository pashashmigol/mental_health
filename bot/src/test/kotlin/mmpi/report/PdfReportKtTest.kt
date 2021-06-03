package mmpi.report

import Gender
import com.soywiz.klock.DateTimeTz
import mmpi.MmpiAnswers
import mmpi.agreeTo
import mmpi.calculateMmpi
import models.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import storage.CentralDataStorage
import telegram.LaunchMode
import java.io.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PdfReportKtTest {

    @BeforeAll
    fun setup() {
        CentralDataStorage.init(
            rootPath = LaunchMode.TESTS.rootPath,
            testingMode = true
        )
    }

    @Test
    fun `generate pdf`() {

        File("build", "test-results").mkdir()
        File("build/test-results", "pdf").mkdir()

        val answers = agreeTo(
            5, 7, 10, 13, 14, 16, 18, 24, 32, 35, 44, 47, 57, 58, 65, 66, 67, 68, 69, 75, 79, 84, 87, 90, 92, 94, 95,
            96, 97, 98, 101, 110, 118, 120, 122, 123, 131, 138, 148, 149, 151, 152, 160, 163, 167, 170, 176, 181, 185,
            186, 187, 188, 193, 194, 195, 196, 198, 205, 210, 215, 216, 220, 223, 224, 228, 230, 233, 235, 240, 243,
            246, 249, 254, 256, 263, 269, 270, 272, 274, 282, 283, 284, 285, 293, 296, 298, 300, 301, 306, 312, 317,
            318, 319, 322, 323, 330, 340, 341, 347, 348, 349, 352, 358, 359, 360, 361, 362, 363, 370, 374, 377
        )

        val result = calculateMmpi(
            answers = answers,
            scales = CentralDataStorage.mmpi377Data.scales(Gender.Female)
        )

        try {
            val pdfFile = File("build/test-results/pdf", "mmpi.pdf")
            pdfFile.createNewFile()
            assertTrue(pdfFile.exists())

            val user = User(id = -1, name = "Test User", googleDriveFolder = "")

            val mmpiAnswers = MmpiAnswers(
                user = user,
                answersList = listOf(),
                date = DateTimeTz.nowLocal(),
                gender = Gender.Female,
            )

            generatePdf(
                questions = listOf(),
                answers = mmpiAnswers,
                result = result,
                pdfStream = pdfFile.outputStream()
            )

        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}