import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import storage.CentralDataStorage
import telegram.LaunchMode
import telegram.launchBots


/**
 * used by App Engine to run bot on cloud
 * */
fun Application.main() {
    val launchMode = LaunchMode.APP_ENGINE
    CentralDataStorage.init(launchMode.rootPath)
    CentralDataStorage.reload()

    val token = Settings.OLD_BOT_TOKEN
    val bot = launchBots(mode = LaunchMode.APP_ENGINE)

    routing {
        get("/status") {
            call.respond("Server is running!")
        }
        post("/$token") {
            val response = call.receiveText()
            bot[token]?.processUpdate(response)
            call.respond(HttpStatusCode.OK)
        }
    }
}