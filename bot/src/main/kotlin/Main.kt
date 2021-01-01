import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.pollAnswer
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.main() {
    routing {
        get("/status") {
            call.respond("Server is running!")
        }
    }
    bot {
        token = "1466693925:AAGkgMILgAg4gvs4Ct6UFlPM5T9VJx0BoIY"
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
    }.startPolling()
}

fun main(){
    bot {
        token = "1466693925:AAGkgMILgAg4gvs4Ct6UFlPM5T9VJx0BoIY"
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
    }.startPolling()
}