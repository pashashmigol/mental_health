package telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.webhook
import io.ktor.util.*
import telegram.helpers.chatInfo

@InternalAPI
fun launchClientBot(
    mode: LaunchMode,
    adminId: Long,
    token: String,
    botsKeeper: () -> BotsKeeper
): Pair<Bot, TelegramRoom> {

    val telegramRoom = TelegramRoom(
        roomId = adminId,
        userConnection = TelegramUserConnection(adminId) { botsKeeper() },
    )

    val clientBot = bot {
        this.token = token

        if (mode == LaunchMode.APP_ENGINE) {
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
                telegramRoom.launchDailyQuiz(chatInfo())
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
        when (mode) {
            LaunchMode.LOCAL -> startPolling()
            LaunchMode.APP_ENGINE -> startWebhook()
            else -> throw IllegalArgumentException("Illegal mode value")
        }
    }
    return Pair(clientBot, telegramRoom)
}