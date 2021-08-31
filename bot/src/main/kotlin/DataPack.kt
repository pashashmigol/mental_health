import lucher.LucherData
import mmpi.MmpiData
import quiz.DailyQuizData
import storage.ReportStorage
import storage.users.AnswerStorage
import storage.users.SessionStorage
import storage.users.UserStorage

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

interface StoragePack {
    val reportStorage: ReportStorage
        get() {
            throw NotImplementedError()
        }
    val userStorage: UserStorage
        get() {
            throw NotImplementedError()
        }
    val sessionStorage: SessionStorage
        get() {
            throw NotImplementedError()
        }
    val answerStorage: AnswerStorage
        get() {
            throw NotImplementedError()
        }
}