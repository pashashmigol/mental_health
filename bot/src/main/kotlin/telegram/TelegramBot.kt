import MyBotConfig.SERVER_HOSTNAME
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.pollAnswer
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.inputmedia.InputMediaPhoto
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import com.github.kotlintelegrambot.webhook
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


            command("mediaGroup") {
                bot.sendMediaGroup(
                    chatId = message.chat.id,
                    mediaGroup = MediaGroup.from(
                        InputMediaPhoto(
                            media = TelegramFile.ByUrl("https://www.sngular.com/wp-content/uploads/2019/11/Kotlin-Blog-1400x411.png"),
                            caption = "I come from an url :P"
                        ),
                        InputMediaPhoto(
                            media = TelegramFile.ByUrl("https://www.sngular.com/wp-content/uploads/2019/11/Kotlin-Blog-1400x411.png"),
                            caption = "Me too!"
                        )
                    ),
                    replyToMessageId = message.messageId
                )
            }
        }
    }.apply {
        when (mode) {
            LaunchMode.LOCAL -> startPolling()
            LaunchMode.APP_ENGINE -> startWebhook()
        }
    }
}