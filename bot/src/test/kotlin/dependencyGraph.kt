import lucher.LucherData
import lucher.loadLucherData
import mmpi.MmpiData
import mmpi.storage.loadMmpiData
import models.TypeOfTest
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import quiz.DailyQuizData
import quiz.loadDailyQuizData
import storage.*
import storage.users.UserStorage

val testDI = DI {
    bind<GoogleDriveConnection>() with singleton {
        GoogleDriveConnection(
            testingMode = true
        )
    }
    bind<UserStorage>() with singleton {
        loadUserStorage(instance())
    }

    bind<GoogleDriveReportStorage>() with singleton {
        loadReportsStorage(instance(), testingMode = true)
    }

    bind<LucherData>() with singleton {
        loadLucherData(instance())
    }

    bind<MmpiData>(TypeOfTest.Mmpi566) with singleton {
        loadMmpiData(instance(), Settings.MMPI_566_QUESTIONS_FILE_ID)
    }

    bind<MmpiData>(TypeOfTest.Mmpi377) with singleton {
        loadMmpiData(instance(), Settings.MMPI_377_QUESTIONS_FILE_ID)
    }

    bind<DailyQuizData>() with singleton {
        loadDailyQuizData(instance(), Settings.DAILY_QUESTIONS_FILE_ID)
    }
}