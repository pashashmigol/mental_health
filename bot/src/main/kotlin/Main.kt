import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import storage.CentralDataStorage
import telegram.LaunchMode
import telegram.launchBot

private const val TOKEN = "1466693925:AAGkgMILgAg4gvs4Ct6UFlPM5T9VJx0BoIY"

/**
 * used by App Engine to run bot on cloud
 * */
fun Application.main() {
    val launchMode = LaunchMode.APP_ENGINE
    CentralDataStorage.reload(launchMode.rootPath)
    val bot = launchBot(mode = LaunchMode.APP_ENGINE, token = TOKEN)

    routing {
        get("/status") {
            call.respond("Server is running!")
        }
        post("/$TOKEN") {
            val response = call.receiveText()
            bot.processUpdate(response)
            call.respond(HttpStatusCode.OK)
        }
    }
}