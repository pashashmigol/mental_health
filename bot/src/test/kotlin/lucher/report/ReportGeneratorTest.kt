package lucher.report

import lucher.LucherAnswers
import lucher.LucherColor
import lucher.calculateResult
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import storage.CentralDataStorage
import telegram.LaunchMode


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ReportGeneratorTest {

    @BeforeAll
    fun init() {
        val launchMode = LaunchMode.TESTS
        CentralDataStorage.init(launchMode.rootPath)
    }

    @Test
    fun generateReport() {
        val answers = LucherAnswers(
            firstRound = listOf(
                LucherColor.Black,
                LucherColor.Red,
                LucherColor.Blue,
                LucherColor.Brown,
                LucherColor.Gray,
                LucherColor.Green,
                LucherColor.Violet,
                LucherColor.Yellow
            ),
            secondRound = listOf(
                LucherColor.Green,
                LucherColor.Blue,
                LucherColor.Red,
                LucherColor.Yellow,
                LucherColor.Black,
                LucherColor.Brown,
                LucherColor.Gray,
                LucherColor.Violet
            )
        )
        val result = calculateResult(answers, CentralDataStorage.lucherData.meanings)
        val report = generateReport(
            userId = "Pasha",
            answers = answers,
            result = result
        )
        println(report)
    }
}