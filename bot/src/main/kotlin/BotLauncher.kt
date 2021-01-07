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

enum class LaunchMode { LOCAL, APP_ENGINE }

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
            pollAnswer {
                println("${pollAnswer.user.firstName} has selected the option " +
                        "${pollAnswer.optionIds.lastOrNull()} in the poll ${pollAnswer.pollId}")

                WorkSpace.onAnswer(this)
            }
            command("mmpiTest") {
                println("mmpiTest")
                WorkSpace.launchMmpiTest(this)
            }
            command("reloadQuestions") {
                println("reloadQuestions")
                CurrentQuestionsProvider.reloadQuestions()
            }
        }
    }.apply {
        when (mode) {
            LaunchMode.LOCAL -> startPolling()
            LaunchMode.APP_ENGINE -> startWebhook()
        }
    }
}