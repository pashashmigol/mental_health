package telegram

import DataPack
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
            val reportStorage: GoogleDriveReportStorage by di.instance()

            val dataPack  = object  : DataPack{
                override val lucherData: LucherData by di.instance()
                override val mmpi566Data: MmpiData by di.instance(TypeOfTest.Mmpi566)
                override val mmpi377Data: MmpiData by di.instance(TypeOfTest.Mmpi377)
                override val dailyQuizData: DailyQuizData by di.instance()
            }

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
                dataPack = dataPack,
                botsKeeper = { botsKeeper!! }
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
    }
}

fun createDependenciesLocal() = DI {
    bind<GoogleDriveConnection>() with singleton {
        GoogleDriveConnection(
            testingMode = true
        )
    }
    bind<UserStorage>() with singleton { loadUserStorage(instance()) }
    bind<GoogleDriveReportStorage>() with singleton { loadReportsStorage(instance(), testingMode = true) }
}

fun createDependenciesProd() = DI {
    bind<GoogleDriveConnection>() with singleton {
        GoogleDriveConnection(
            testingMode = false
        )
    }
    bind<UserStorage>() with singleton { loadUserStorage(instance()) }
    bind<GoogleDriveReportStorage>() with singleton { loadReportsStorage(instance(), testingMode = false) }
}