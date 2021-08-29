package telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.webhook
import io.ktor.util.*
import lucher.LucherData
import mmpi.MmpiData
import quiz.DailyQuizData
import quiz.DailyQuizSession
import storage.ReportStorage
import storage.users.UserStorage
import telegram.helpers.chatInfo

@InternalAPI
fun launchClientBot(
    adminId: Long,
    token: String,
    userConnection: UserConnection,
    userStorage: UserStorage,
    reportStorage: ReportStorage,
    lusherData: LucherData,
    mmpiData566: MmpiData,
    mmpiData377: MmpiData,
    dailyQuizData: DailyQuizData,
    botsKeeper: () -> BotsKeeper
): Pair<Bot, TelegramRoom> {

    val telegramRoom = TelegramRoom(
        roomId = adminId,
        userConnection = userConnection,
        reportStorage = reportStorage,
        userStorage = userStorage,
        lusherData = lusherData,
        mmpiData566 = mmpiData566,
        mmpiData377 = mmpiData377,
        dailyQuizData = dailyQuizData,
    )

    val clientBot = bot {
        this.token = token

        if (LaunchMode.current == LaunchMode.APP_ENGINE) {
            webhook {
                url = "${Settings.SERVER_HOSTNAME}/$token"
                maxConnections = 50
                allowedUpdates = listOf("message", "poll_answer")
            }
        }
        dispatch {
            command("mmpi566") {
                println("mmpi566")
                telegramRoom.launchMmpi566(chatInfo())
            }
            command("mmpi377") {
                println("mmpi377")
                telegramRoom.launchMmpi377(chatInfo())
            }
            command("lucher") {
                println("lucher")
                telegramRoom.launchLucher(chatInfo())
            }
            command("quiz") {
                println("quiz")
                telegramRoom.launchDailyQuiz(chatInfo(), DailyQuizSession.Time.MORNING)
            }
            command("start") {
                telegramRoom.welcomeUser(
                    chatInfo(),
                    TelegramUserConnection(adminId) { botsKeeper() }
                )
            }
            command("restoreState") {
                telegramRoom.restoreState()
            }
            callbackQuery {
                telegramRoom.onCallbackQuery(
                    chatInfo = chatInfo(),
                    data = this.callbackQuery.data
                )
            }
            message {
                telegramRoom.onMessage(
                    chatInfo = chatInfo(),
                    message = this.message.text
                )
            }
        }
    }.apply {
        when (LaunchMode.current) {
            LaunchMode.LOCAL -> startPolling()
            LaunchMode.APP_ENGINE -> startWebhook()
            else -> throw IllegalArgumentException("Illegal mode value")
        }
    }
    return Pair(clientBot, telegramRoom)
}