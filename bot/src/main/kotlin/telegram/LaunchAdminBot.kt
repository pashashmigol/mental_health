package telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.webhook
import storage.users.UserStorage
import telegram.helpers.showUsersList

fun launchAdminBot(
    token: String,
    userStorage: UserStorage
): Bot {
    return bot {
        this.token = token
        if (LaunchMode.current == LaunchMode.APP_ENGINE) {
            webhook {
                url = "${Settings.SERVER_HOSTNAME}/$token"
                maxConnections = 50
                allowedUpdates = listOf("message", "poll_answer")
            }
        }
        dispatch {
            command("users") {
                showUsersList(
                    bot = bot,
                    userId = message.from!!.id,
                    userStorage = userStorage
                )
            }
            command("reload") {
//                CentralDataStorage.reload(connection)
            }
        }
    }.apply {
        when (LaunchMode.current) {
            LaunchMode.LOCAL -> startPolling()
            LaunchMode.APP_ENGINE -> startWebhook()
            else -> throw IllegalArgumentException("Illegal mode value")
        }
    }
}