import MyBotConfig.SERVER_HOSTNAME
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.pollAnswer
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.webhook
import lucher.LucherSession
import mmpi.CurrentQuestionsProvider
import telegram.TelegramRoom


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
                println(
                    "${pollAnswer.user.firstName} has selected the option " +
                            "${pollAnswer.optionIds.lastOrNull()} in the poll ${pollAnswer.pollId}"
                )

                TelegramRoom.onAnswer(this)
            }
            command("mmpi") {
                println("mmpi")
                TelegramRoom.launchMmpiTest(this)
            }
            command("reload") {
                println("reloadQuestions")
                CurrentQuestionsProvider.reload()
            }

            command("test") {
                println("test call")
                TelegramRoom.makeMockTest(this)
            }

            command("html1") {
                val markdownV2Text = """
                  <a href='https://twitter.com/jordanbpeterson'>Jordan B. Peterson</a>
                """.trimIndent()
                bot.sendMessage(
                    chatId = message.chat.id,
                    text = markdownV2Text,
                    parseMode = ParseMode.HTML
                )
            }

            command("lucher") {
                LucherSession.startTest(this)
            }

            callbackQuery {
                LucherSession.onCallBack(this)
            }
        }
    }.apply {
        when (mode) {
            LaunchMode.LOCAL -> startPolling()
            LaunchMode.APP_ENGINE -> startWebhook()
        }
    }
}