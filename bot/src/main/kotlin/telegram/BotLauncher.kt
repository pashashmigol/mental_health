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

class BotLauncher(
    val mode: LaunchMode,
    val tokens: List<Tokens>
) {
    fun launchBots(): List<BotsKeeper> {
        return tokens.map {
            var botsKeeper: BotsKeeper? = null

            val adminBot = launchAdminBot(mode, it.ADMIN)

            val (clientBot, telegramRoom) = launchClientBot(
                mode, it.ADMIN_ID, it.CLIENT, lazy { botsKeeper!! }.value
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

private fun launchAdminBot(
    mode: LaunchMode,
    token: String,
): Bot {
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
            command("users") {
                showUsersList(bot, message.from!!.id)
            }
            command("reload") {
                CentralDataStorage.reload()
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
): Pair<Bot, TelegramRoom> {

    val telegramRoom = TelegramRoom(
        roomId = adminId,
        userConnection = TelegramUserConnection(adminId) { botsKeeper },
    )

    val clientBot = bot {
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
    return Pair(clientBot, telegramRoom)
}