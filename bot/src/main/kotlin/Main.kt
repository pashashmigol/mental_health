import Settings.ADMIN_BOT_TOKEN
import Settings.CLIENT_BOT_TOKEN
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import storage.CentralDataStorage
import telegram.BotsKeeper
import telegram.LaunchMode
import telegram.launchBots


/**
 * used by App Engine to run bot on cloud
 * */
fun Application.main() {
    val launchMode = LaunchMode.APP_ENGINE
    CentralDataStorage.init(launchMode.rootPath)
    CentralDataStorage.reload()

    launchBots(mode = LaunchMode.APP_ENGINE)

    routing {
        get("/status") {
            call.respond("Server is running!")
        }
        post("/$ADMIN_BOT_TOKEN") {
            val response = call.receiveText()
            BotsKeeper.adminBot.processUpdate(response)
            call.respond(HttpStatusCode.OK)
        }
        post("/$CLIENT_BOT_TOKEN") {
            val response = call.receiveText()
            BotsKeeper.clientBot.processUpdate(response)
            call.respond(HttpStatusCode.OK)
        }
    }
}