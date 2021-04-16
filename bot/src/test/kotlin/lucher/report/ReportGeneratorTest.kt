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
        val result = calculateResult(answers, CentralDataStorage.lucherData.meanings)
        val report = generateReport(
            userId = "Pasha",
            answers = answers,
            result = result
        )
        println(report)
    }
}