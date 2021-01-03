import MyBotConfig.SERVER_HOSTNAME
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.pollAnswer
import com.github.kotlintelegrambot.webhook


object MyBotConfig {
    const val SERVER_HOSTNAME = "mental-health-300314.oa.r.appspot.com"
}

fun launchBot(testingMode: Boolean, token: String) : Bot {
    return bot {
        this.token = token
        if (!testingMode) {
            webhook {
                url = "${SERVER_HOSTNAME}/$token"
                maxConnections = 50
                allowedUpdates = listOf("message", "poll_answer")
            }
        }
        dispatch {
            pollAnswer {
                println("${pollAnswer.user.firstName} has selected the option " +
                        "${pollAnswer.optionIds.lastOrNull()} in the poll ${pollAnswer.pollId}")

                WorkSpace.onAnswer(this)
            }
            command("mockTest") {
                WorkSpace.launchMockTest(this)
            }
        }
    }.apply {
        if(testingMode) {
            startPolling()
        } else {
            startWebhook()
        }
    }
}