package reports

import mmpi.MmpiProcess
import mmpi.Scale
import org.junit.jupiter.api.Test


internal class ReportHelpersTest {

    @Test
    fun chartFor() {
        val mmpi = MmpiProcess.Result(
            description = "",
            liesScale = Scale.Result(name = "lies", score = 120, description = ""),
            credibilityScale = Scale.Result(name = "credibility", score = 0, description = ""),
            correctionScale = Scale.Result(name = "correction", score = 60, description = ""),
            introversionScale0 = Scale.Result(name = "introversion", score = 50, description = ""),
            overControlScale1 = Scale.Result(name = "overControl", score = 50, description = ""),
            passivityScale2 = Scale.Result(name = "passivity", score = 50, description = ""),
            labilityScale3 = Scale.Result(name = "lability", score = 50, description = ""),
            impulsivenessScale4 = Scale.Result(name = "impulsiveness", score = 50, description = ""),
            masculinityScale5 = Scale.Result(name = "masculinity", score = 50, description = ""),
            rigidityScale6 = Scale.Result(name = "rigidity", score = 50, description = ""),
            anxietyScale7 = Scale.Result(name = "anxiety", score = 50, description = ""),
            individualismScale8 = Scale.Result(name = "individualism", score = 50, description = ""),
            optimismScale9 = Scale.Result(name = "optimism", score = 50, description = "")
        )
        val chartSvg = chartFor(mmpi)
        println(chartSvg)
    }
}