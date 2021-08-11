package lucher.report

import com.soywiz.klock.DateTimeTz
import lucher.LucherAnswersContainer
import lucher.LucherColor
import lucher.calculateLucher
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import storage.CentralDataStorage
import telegram.LaunchMode
import models.User

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ReportGeneratorTest {

    @BeforeAll
    fun init() {
        CentralDataStorage.init(
            launchMode = LaunchMode.TESTS,
            testingMode = true
        )
    }

    @Test
    fun generateReport() {
        val answers = LucherAnswersContainer(
            user = User(
                id = 0,
                name = "ReportGeneratorTest",
                googleDriveFolderUrl = "",
                googleDriveFolderId = ""
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
        val result = calculateLucher(answers, CentralDataStorage.lucherData.meanings)
        val report = generateReport(
            userId = "Pasha",
            answers = answers,
            result = result
        )
        println(report)
    }
}