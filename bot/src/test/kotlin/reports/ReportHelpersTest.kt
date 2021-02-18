package reports

import mmpi.MmpiProcess
import mmpi.Scale
import org.junit.jupiter.api.Test


internal class ReportHelpersTest {

    @Test
    fun chartFor() {
        val mmpi = MmpiProcess.Result(
            description = "",
            liesScale = Scale.Result(name = "lies", score = 20, description = ""),
            credibilityScale = Scale.Result(name = "credibility", score = 30, description = ""),
            introversionScale = Scale.Result(name = "introversion", score = 40, description = ""),
            overControlScale1 = Scale.Result(name = "overControl", score = 60, description = ""),
            passivityScale2 = Scale.Result(name = "passivity", score = 50, description = ""),
            labilityScale3 = Scale.Result(name = "lability", score = 90, description = ""),
            impulsivenessScale4 = Scale.Result(name = "impulsiveness", score = 110, description = ""),
            masculinityScale5 = Scale.Result(name = "masculinity", score = 40, description = ""),
            rigidityScale6 = Scale.Result(name = "rigidity", score = 30, description = ""),
            anxietyScale7 = Scale.Result(name = "anxiety", score = 70, description = ""),
            individualismScale8 = Scale.Result(name = "individualism", score = 60, description = ""),
            optimismScale9 = Scale.Result(name = "optimism", score = 10, description = "")
        )
        val chartSvg = chartFor(mmpi)
        println(chartSvg)
    }
}