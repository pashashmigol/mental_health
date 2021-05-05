package telegram

import Settings.SERVER_HOSTNAME
import Tokens
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.webhook
import storage.CentralDataStorage
import telegram.helpers.chatInfo
import telegram.helpers.showUsersList


fun launchBots(mode: LaunchMode, tokens: Tokens): BotsKeeper {
    val botsKeeper = BotsKeeper()

    botsKeeper.adminBot = launchAdminBot(mode, tokens.ADMIN_ID, tokens.ADMIN, botsKeeper)
    botsKeeper.clientBot = launchClientBot(mode, tokens.ADMIN_ID, tokens.CLIENT, botsKeeper)

    return botsKeeper
}

private fun launchAdminBot(
    mode: LaunchMode,
    adminId: Long,
    token: String,
    botsKeeper: BotsKeeper
): Bot {
    return bot {
        val telegramRoom = TelegramRoom(
            TelegramUserConnection(adminId) { botsKeeper },
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

private fun launchClientBot(
    mode: LaunchMode,
    adminId: Long,
    token: String,
    botsKeeper: BotsKeeper
): Bot {
    return bot {
        val telegramRoom = TelegramRoom(
            TelegramUserConnection(adminId) { botsKeeper },
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
                telegramRoom.welcomeUser(
                    chatInfo(),
                    TelegramUserConnection(adminId) { botsKeeper }
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