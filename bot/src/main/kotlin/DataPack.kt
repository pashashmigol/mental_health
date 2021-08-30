import lucher.LucherData
import mmpi.MmpiData
import quiz.DailyQuizData

interface DataPack {
    val lucherData: LucherData
        get() {
            throw NotImplementedError()
        }
    val mmpi566Data: MmpiData
        get() {
            throw NotImplementedError()
        }
    val mmpi377Data: MmpiData
        get() {
            throw NotImplementedError()
        }
    val dailyQuizData: DailyQuizData
        get() {
            throw NotImplementedError()
        }
}