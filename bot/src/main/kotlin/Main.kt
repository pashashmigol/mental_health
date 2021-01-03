import MyBotConfig.API_TOKEN
import MyBotConfig.SERVER_HOSTNAME
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.pollAnswer
import com.github.kotlintelegrambot.webhook
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*


fun Application.main() {
    val bot = startBot()

    routing {
        get("/status") {
            call.respond("Server is running!")
        }
        post("/$API_TOKEN") {
            val response = call.receiveText()
            bot.processUpdate(response)
            call.respond(HttpStatusCode.OK)
        }
    }
}

object MyBotConfig {
    const val API_TOKEN = "1466693925:AAGkgMILgAg4gvs4Ct6UFlPM5T9VJx0BoIY"
    const val SERVER_HOSTNAME = "mental-health-300314.oa.r.appspot.com"
}

fun startBot() : Bot {
    return bot {
        token = API_TOKEN
        webhook {
            url = "${SERVER_HOSTNAME}/$API_TOKEN"
            maxConnections = 50
            allowedUpdates = listOf("message", "poll_answer")
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
    }.apply { startWebhook() }
}