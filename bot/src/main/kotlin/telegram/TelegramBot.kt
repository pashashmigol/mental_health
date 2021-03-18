package telegram

import Settings.ADMIN_BOT_TOKEN
import Settings.CLIENT_BOT_TOKEN
import Settings.SERVER_HOSTNAME
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.webhook
import storage.CentralDataStorage
import telegram.helpers.showUsersList


fun launchBots(mode: LaunchMode): Map<String, Bot> {

    val adminBotToken: String
    val clientBotToken: String

    when (mode) {
        LaunchMode.APP_ENGINE -> {
            adminBotToken = ADMIN_BOT_TOKEN
            clientBotToken = CLIENT_BOT_TOKEN
        }
        LaunchMode.LOCAL, LaunchMode.TESTS -> {
            adminBotToken = ADMIN_BOT_TOKEN
            clientBotToken = CLIENT_BOT_TOKEN
        }
    }
    val adminBot = launchAdminBot(mode, adminBotToken)
    val clientBot = launchClientBot(mode, clientBotToken)

    return mapOf(adminBotToken to adminBot, clientBotToken to clientBot)
}

private fun launchAdminBot(mode: LaunchMode, token: String): Bot {
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
            command("users") {
                showUsersList(bot, message.from!!.id)
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
            else -> throw IllegalArgumentException("Illegal mode value")
        }
    }
}

private fun launchClientBot(mode: LaunchMode, token: String): Bot {
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
            command("lucher") {
                TelegramRoom.launchLucherTest(this)
            }
            command("start") {
                TelegramRoom.welcomeNewUser(this)
            }
            callbackQuery {
                TelegramRoom.callbackQuery(this)
            }
        }
    }.apply {
        when (mode) {
            LaunchMode.LOCAL -> startPolling()
            LaunchMode.APP_ENGINE -> startWebhook()
            else -> throw IllegalArgumentException("Illegal mode value")
        }
    }
}