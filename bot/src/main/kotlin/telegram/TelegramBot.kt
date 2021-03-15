package telegram

import telegram.MyBotConfig.SERVER_HOSTNAME
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.webhook
import storage.CentralDataStorage


object MyBotConfig {
    const val SERVER_HOSTNAME = "mental-health-300314.oa.r.appspot.com"
}

fun launchBot(mode: LaunchMode, token: String): Bot {
    return bot {
        this.token = token
        if (mode == LaunchMode.APP_ENGINE) {
            webhook {
                url = "${SERVER_HOSTNAME}/$token"
                maxConnections = 50
                allowedUpdates = listOf("message", "poll_answer")
            }
        }
        dispatch {
            command("mmpi566") {
                println("mmpi566")
                TelegramRoom.launchMmpi566Test(this)
            }
            command("mmpi377") {
                println("mmpi377")
                TelegramRoom.launchMmpi377Test(this)
            }
            command("mmpiTest") {
                println("mmpiTest")
                TelegramRoom.launchMmpiMockTest(this)
            }
            command("lucher") {
                TelegramRoom.launchLucherTest(this)
            }
            command("reload") {
                println("reloadQuestions")
                CentralDataStorage.reload()
            }
            callbackQuery {
                TelegramRoom.callbackQuery(this)
            }
        }
    }.apply {
        when (mode) {
            LaunchMode.LOCAL -> startPolling()
            LaunchMode.APP_ENGINE -> startWebhook()
        }
    }
}