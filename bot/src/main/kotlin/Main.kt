import com.google.appengine.api.LifecycleManager
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import storage.CentralDataStorage
import telegram.*

/**
 * used by App Engine to run bot on cloud
 * */
@Suppress("unused") // Referenced in application.conf
@KtorExperimentalAPI
@InternalAPI
fun Application.main() {
    val launchMode = LaunchMode.APP_ENGINE
    CentralDataStorage.init(launchMode)

    val botLauncher = BotLauncher(
        mode = LaunchMode.APP_ENGINE,
        tokens = PRODUCTION_TOKENS
    )
    val botsKeepers = botLauncher.launchBots()

    bindToServerEvents(botsKeepers)
    bindTelegramWebhooks(botsKeepers)
    bindTToStartStopEvents(botsKeepers)
}


@InternalAPI
private fun Application.bindTToStartStopEvents(keepers: List<BotsKeeper>) {

    routing {
        get("/status") {
            call.respond("Server is running!")
        }
        get("/_ah/stop") {
            call.respond("Server is stopping!")
            notifyAdmins(keepers, "/_ah/stop is called")

            keepers.forEach{
                it.room.saveState()
            }
        }
        get("/_ah/start") {
            call.respond("Server is starting!")
            notifyAdmins(keepers, "/_ah/start is called")

            keepers.forEach{
                it.room.restoreState()
            }
        }
    }
}

@InternalAPI
private fun Application.bindTelegramWebhooks(keepers: List<BotsKeeper>) {
    routing {
        keepers.forEach { keeper ->
            post("/${keeper.tokens.ADMIN}") {
                val response = call.receiveText()
                keeper.adminBot.processUpdate(response)
                call.respond(HttpStatusCode.OK)
            }
            post("/${keeper.tokens.CLIENT}") {
                val response = call.receiveText()
                keeper.clientBot.processUpdate(response)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

@InternalAPI
private fun Application.bindToServerEvents(keepers: List<BotsKeeper>) {
    environment.apply {
        monitor.subscribe(ApplicationStarted) {
            notifyAdmins(keepers, "environment.monitor.subscribe(ApplicationStarted)")
        }
        monitor.subscribe(ApplicationStopPreparing) {
            notifyAdmins(keepers, "environment.monitor.subscribe(ApplicationStopPreparing)")
        }
        monitor.subscribe(ApplicationStopping) {
            notifyAdmins(keepers, "environment.monitor.subscribe(ApplicationStopping)")
        }
        monitor.subscribe(ApplicationStopped) {
            notifyAdmins(keepers, "environment.monitor.subscribe(ApplicationStopped)")
        }
        LifecycleManager.getInstance().setShutdownHook {
            notifyAdmins(keepers, "LifecycleManager.getInstance().setShutdownHook")
        }
    }
}

@InternalAPI
private fun notifyAdmins(keepers: List<BotsKeeper>, message: String) {
    keepers.forEach { keeper ->
        keeper.adminBot.notifyAdmin(
            keeper.tokens.ADMIN_ID, message
        )
    }
}