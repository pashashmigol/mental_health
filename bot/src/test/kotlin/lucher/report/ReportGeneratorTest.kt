package lucher.report

import com.soywiz.klock.DateTimeTz
import lucher.LucherAnswersContainer
import lucher.LucherColor
import lucher.LucherData
import lucher.calculateLucher
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import models.User
import org.kodein.di.instance
import testDI

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ReportGeneratorTest {

    private val lucherData: LucherData by testDI.instance()

    @Test
    fun generateReport() {
        val answers = LucherAnswersContainer(
            user = User(
                id = 0,
                name = "ReportGeneratorTest",
                googleDriveFolderUrl = "",
                googleDriveFolderId = "",
                runDailyQuiz = false
            ),
            date = DateTimeTz.nowLocal(),
            firstRound = listOf(
                LucherColor.Red,
                LucherColor.Blue,
                LucherColor.Green,
                LucherColor.Yellow,
                LucherColor.Brown,
                LucherColor.Gray,
                LucherColor.Violet,
                LucherColor.Black,
            ),
            secondRound = listOf(
                LucherColor.Green,
                LucherColor.Red,
                LucherColor.Blue,
                LucherColor.Yellow,
                LucherColor.Brown,
                LucherColor.Gray,
                LucherColor.Violet,
                LucherColor.Black,
            )
        )
        val result = calculateLucher(answers, lucherData.meanings)
        val report = generateReport(
            userId = "Pasha",
            answers = answers,
            result = result
        )
        println(report)
    }
}