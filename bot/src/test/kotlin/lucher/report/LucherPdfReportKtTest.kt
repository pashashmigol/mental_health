package lucher.report

import com.soywiz.klock.DateTimeTz
import lucher.LucherAnswersContainer
import lucher.LucherColor
import lucher.LucherData
import lucher.calculateLucher
import models.User
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.kodein.di.instance
import testDI
import java.io.File


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LucherPdfReportKtTest {
    private val lucherData: LucherData by testDI.instance()

    @Test
    fun pdfReportLucher() {
        File("build", "test-results").mkdir()
        File("build/test-results", "pdf").mkdir()

        val user = User(
            id = -1,
            name = "Test User",
            googleDriveFolderUrl = "",
            googleDriveFolderId = "",
            runDailyQuiz = false
        )

        val answers = LucherAnswersContainer(
            user = user,
            date = DateTimeTz.nowLocal(),
            firstRound = LucherColor.values().toList(),
            secondRound = LucherColor.values().toList()
        )

        val result = calculateLucher(
            answers = answers,
            meanings = lucherData.meanings
        )

        try {
            val pdfFile = File("build/test-results/pdf", "lucher.pdf")
            pdfFile.createNewFile()
            assertTrue(pdfFile.exists())

            val bytes = pdfReportLucher(
                answers = answers,
                result = result,
            )
            pdfFile.writeBytes(bytes)

        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}