package telegram

import Tokens
import io.ktor.util.*
import lucher.LucherData
import mmpi.MmpiData
import models.TypeOfTest
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import quiz.DailyQuizData
import storage.*
import storage.users.UserStorage


@InternalAPI
class BotLauncher(
    val tokens: List<Tokens>
) {
    fun launchBots(): List<BotsKeeper> {
        return tokens.map {
            var botsKeeper: BotsKeeper? = null

            val di = createDependenciesContainer()

            val userStorage: UserStorage by di.instance()
            val reportStorage: ReportStorage by di.instance()

            val lucherData: LucherData by di.instance()
            val mmpiData566: MmpiData by di.instance(TypeOfTest.Mmpi566)
            val mmpiData377: MmpiData by di.instance(TypeOfTest.Mmpi377)
            val dailyQuizData: DailyQuizData by di.instance()


            val adminBot = launchAdminBot(
                token = it.ADMIN,
                userStorage = userStorage
            )

            val (clientBot, telegramRoom) = launchClientBot(
                adminId = it.ADMIN_ID,
                token = it.CLIENT,
                userConnection = TelegramUserConnection(it.ADMIN_ID) { botsKeeper!! },
                userStorage = userStorage,
                reportStorage = reportStorage,
                lusherData = lucherData,
                botsKeeper = { botsKeeper!! },
                mmpiData566 = mmpiData566,
                mmpiData377 = mmpiData377,
                dailyQuizData = dailyQuizData
            )

            botsKeeper = BotsKeeper(
                tokens = it,
                adminBot = adminBot,
                clientBot = clientBot,
                room = telegramRoom
            )
            botsKeeper
        }
    }
}

fun createDependenciesContainer(): DI {
    return when (LaunchMode.current) {
        LaunchMode.LOCAL -> createDependenciesLocal()
        LaunchMode.TESTS -> createDependenciesProd()
        LaunchMode.APP_ENGINE -> throw NotImplementedError()
        else -> throw IllegalStateException()
    }
}

fun createDependenciesLocal() = DI {
    bind<GoogleDriveConnection>() with singleton {
        GoogleDriveConnection(
            testingMode = true
        )
    }
    bind<UserStorage>() with singleton { loadUserStorage(instance()) }
    bind<ReportStorage>() with singleton { loadReportsStorage(instance(), testingMode = true) }
}

fun createDependenciesProd() = DI {
    bind<GoogleDriveConnection>() with singleton {
        GoogleDriveConnection(
            testingMode = false
        )
    }
    bind<UserStorage>() with singleton { loadUserStorage(instance()) }
    bind<ReportStorage>() with singleton { loadReportsStorage(instance(), testingMode = false) }
}