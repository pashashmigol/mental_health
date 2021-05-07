package mmpi.reports

import mmpi.MmpiProcess
import mmpi.Scale
import mmpi.report.generateReport
import models.User
import org.junit.jupiter.api.Test


internal class ReportHelpersTest {

    @Test
    fun chartFor() {
        val mmpiResult = MmpiProcess.Result(
            liesScaleL = Scale.Result(name = "lies", score = 120, description = "", raw = 0),
            credibilityScaleF = Scale.Result(name = "credibility", score = 0, description = "", raw = 0),
            correctionScaleK = Scale.Result(name = "correction", score = 60, description = "", raw = 0),
            introversionScale0 = Scale.Result(name = "introversion", score = 50, description = "", raw = 0),
            overControlScale1 = Scale.Result(name = "overControl", score = 50, description = "", raw = 0),
            passivityScale2 = Scale.Result(name = "passivity", score = 50, description = "", raw = 0),
            labilityScale3 = Scale.Result(name = "lability", score = 50, description = "", raw = 0),
            impulsivenessScale4 = Scale.Result(name = "impulsiveness", score = 20, description = "", raw = 0),
            masculinityScale5 = Scale.Result(name = "masculinity", score = 50, description = "", raw = 0),
            rigidityScale6 = Scale.Result(name = "rigidity", score = 50, description = "", raw = 0),
            anxietyScale7 = Scale.Result(name = "anxiety", score = 50, description = "", raw = 0),
            individualismScale8 = Scale.Result(name = "individualism", score = 50, description = "", raw = 0),
            optimismScale9 = Scale.Result(name = "optimism", score = 50, description = "", raw = 0)
        )

        val report = generateReport(
            user = User(id = 0, name = "Pasha Shmyhol"),
            questions = listOf(),
            answers = listOf(),
            result = mmpiResult
        )
        println(report)
    }
}