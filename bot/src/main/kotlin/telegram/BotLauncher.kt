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
                TelegramRoom.launchMmpi566Test(
                    chatInfo(),
                    TelegramUserConnection(BotsKeeper.clientBot),
                    TelegramUserConnection(BotsKeeper.adminBot)
                )
            }
            command("mmpi377") {
                println("mmpi377")
                TelegramRoom.launchMmpi377Test(
                    chatInfo(),
                    TelegramUserConnection(BotsKeeper.clientBot),
                    TelegramUserConnection(BotsKeeper.adminBot)
                )
            }
            command("mmpiTest") {
                println("mmpiTest")
                TelegramRoom.launchMmpiMockTest(
                    chatInfo(),
                    TelegramUserConnection(BotsKeeper.clientBot),
                    TelegramUserConnection(BotsKeeper.adminBot)
                )
            }
            command("lucher") {

                TelegramRoom.launchLucherTest(
                    chatInfo = chatInfo(),
                    TelegramUserConnection(BotsKeeper.clientBot),
                    TelegramUserConnection(BotsKeeper.adminBot)
                )
            }
            command("users") {
                showUsersList(bot, message.from!!.id)
            }
            command("reload") {
                CentralDataStorage.reload()
            }
            callbackQuery {
                TelegramRoom.callbackQuery(
                    chatInfo = chatInfo(),
                    data = this.callbackQuery.data,
                    TelegramUserConnection(BotsKeeper.clientBot),
                    TelegramUserConnection(BotsKeeper.adminBot)
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
                TelegramRoom.launchMmpi566Test(chatInfo(),
                    TelegramUserConnection(BotsKeeper.clientBot),
                    TelegramUserConnection(BotsKeeper.adminBot))
            }
            command("mmpi377") {
                println("mmpi377")
                TelegramRoom.launchMmpi377Test(chatInfo(),
                    TelegramUserConnection(BotsKeeper.clientBot),
                    TelegramUserConnection(BotsKeeper.adminBot))
            }
            command("lucher") {
                TelegramRoom.launchLucherTest(
                    chatInfo = chatInfo(),
                    TelegramUserConnection(BotsKeeper.clientBot),
                    TelegramUserConnection(BotsKeeper.adminBot)
                )
            }
            command("start") {
                TelegramRoom.welcomeNewUser(chatInfo(), TelegramUserConnection(this))
            }
            callbackQuery {
                TelegramRoom.callbackQuery(
                    chatInfo = chatInfo(),
                    data = this.callbackQuery.data,
                    TelegramUserConnection(BotsKeeper.clientBot),
                    TelegramUserConnection(BotsKeeper.adminBot)
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