import Settings.ADMIN_BOT_TOKEN
import Settings.CLIENT_BOT_TOKEN
import com.google.appengine.api.LifecycleManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import storage.CentralDataStorage
import telegram.BotsKeeper
import telegram.LaunchMode
import telegram.launchBots
import telegram.notifyAdmin

/**
 * used by App Engine to run bot on cloud
 * */
@Suppress("unused") // Referenced in application.conf
@KtorExperimentalAPI
fun Application.main() {
    val launchMode = LaunchMode.APP_ENGINE
    CentralDataStorage.init(launchMode.rootPath)

    launchBots(mode = LaunchMode.APP_ENGINE)

    environment.monitor.subscribe(ApplicationStarted) {
        print("###(ApplicationStarted)")
        notifyAdmin("environment.monitor.subscribe(ApplicationStarted)")
    }

    environment.monitor.subscribe(ApplicationStopPreparing) {
        println("environment.monitor.subscribe(ApplicationStopPreparing)")
        notifyAdmin("environment.monitor.subscribe(ApplicationStopPreparing)")
    }

    environment.monitor.subscribe(ApplicationStopping) {
        println("environment.monitor.subscribe(ApplicationStopping)")
        notifyAdmin("environment.monitor.subscribe(ApplicationStopping)")
    }

    environment.monitor.subscribe(ApplicationStopped) {
        println("environment.monitor.subscribe(ApplicationStopping)")
        notifyAdmin("environment.monitor.subscribe(ApplicationStopped)")
    }

    LifecycleManager.getInstance().setShutdownHook {
        print("###(setShutdownHook)")
        notifyAdmin("LifecycleManager.getInstance().setShutdownHook")
    }

    routing {
        get("/status") {
            call.respond("Server is running!")
        }
        get("/_ah/stop") {
            call.respond("Server is stopping!")
            notifyAdmin("/_ah/stop is called")
        }
        get("/_ah/start") {
            call.respond("Server is starting!")
            notifyAdmin("/_ah/start is called")
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