package telegram

import Settings.ADMIN_BOT_TOKEN
import Settings.ADMIN_BOT_TOKEN_TESTING
import Settings.CLIENT_BOT_TOKEN
import Settings.CLIENT_BOT_TOKEN_TESTING
import Settings.SERVER_HOSTNAME
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.webhook
import storage.CentralDataStorage
import telegram.helpers.chatInfo
import telegram.helpers.showUsersList


fun launchBots(mode: LaunchMode) {

    val adminBotToken: String
    val clientBotToken: String

    when (mode) {
        LaunchMode.APP_ENGINE -> {
            adminBotToken = ADMIN_BOT_TOKEN
            clientBotToken = CLIENT_BOT_TOKEN
        }
        LaunchMode.LOCAL, LaunchMode.TESTS -> {
            adminBotToken = ADMIN_BOT_TOKEN_TESTING
            clientBotToken = CLIENT_BOT_TOKEN_TESTING
        }
    }
    val adminBot = launchAdminBot(mode, adminBotToken)
    val clientBot = launchClientBot(mode, clientBotToken)

    BotsKeeper.adminBot = adminBot
    BotsKeeper.clientBot = clientBot
}

private fun launchAdminBot(mode: LaunchMode, token: String): Bot {
    return bot {
        val telegramRoom = TelegramRoom(
            TelegramUserConnection { BotsKeeper.clientBot },
            TelegramUserConnection { BotsKeeper.adminBot }
        )
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
                telegramRoom.launchMmpi566Test(chatInfo())
            }
            command("mmpi377") {
                println("mmpi377")
                telegramRoom.launchMmpi377Test(chatInfo())
            }
            command("lucher") {
                telegramRoom.launchLucherTest(chatInfo())
            }
            command("users") {
                showUsersList(bot, message.from!!.id)
            }
            command("reload") {
                CentralDataStorage.reload()
            }
            callbackQuery {
                telegramRoom.callbackQuery(
                    chatInfo = chatInfo(),
                    data = this.callbackQuery.data
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
}

private fun launchClientBot(mode: LaunchMode, token: String): Bot {
    return bot {
        val telegramRoom = TelegramRoom(
            TelegramUserConnection { BotsKeeper.clientBot },
            TelegramUserConnection { BotsKeeper.adminBot }
        )
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
                telegramRoom.launchMmpi566Test(
                    chatInfo()
                )
            }
            command("mmpi377") {
                println("mmpi377")
                telegramRoom.launchMmpi377Test(
                    chatInfo()
                )
            }
            command("lucher") {
                telegramRoom.launchLucherTest(chatInfo())
            }
            command("start") {
                telegramRoom.welcomeNewUser(
                    chatInfo(),
                    TelegramUserConnection { this.bot }
                )
            }
            callbackQuery {
                telegramRoom.callbackQuery(
                    chatInfo = chatInfo(),
                    data = this.callbackQuery.data
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
}