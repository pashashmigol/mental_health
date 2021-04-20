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

    val botsKeepers = PRODUCTION_TOKENS.map { tokens ->
        val botsKeeper = launchBots(mode = LaunchMode.APP_ENGINE, tokens)
        Pair(botsKeeper, tokens)
    }

    botsKeepers.forEach { (botsKeeper, tokens) ->
        launch(botsKeeper, tokens)
    }
}

private fun Application.launch(botsKeeper: BotsKeeper, tokens: Tokens) {
    val adminBot = botsKeeper.adminBot

    environment.apply {
        monitor.subscribe(ApplicationStarted) {
            print("###(ApplicationStarted)")
            adminBot.notifyAdmin(tokens.ADMIN_ID, "environment.monitor.subscribe(ApplicationStarted)")
        }

        monitor.subscribe(ApplicationStopPreparing) {
            println("environment.monitor.subscribe(ApplicationStopPreparing)")
            adminBot.notifyAdmin(tokens.ADMIN_ID, "environment.monitor.subscribe(ApplicationStopPreparing)")
        }

        monitor.subscribe(ApplicationStopping) {
            println("environment.monitor.subscribe(ApplicationStopping)")
            adminBot.notifyAdmin(tokens.ADMIN_ID, "environment.monitor.subscribe(ApplicationStopping)")
        }

        monitor.subscribe(ApplicationStopped) {
            println("environment.monitor.subscribe(ApplicationStopping)")
            adminBot.notifyAdmin(tokens.ADMIN_ID, "environment.monitor.subscribe(ApplicationStopped)")
        }

        LifecycleManager.getInstance().setShutdownHook {
            print("###(setShutdownHook)")
            adminBot.notifyAdmin(tokens.ADMIN_ID, "LifecycleManager.getInstance().setShutdownHook")
        }
    }

    routing {
        get("/status") {
            call.respond("Server is running!")
        }
        get("/_ah/stop") {
            call.respond("Server is stopping!")
            adminBot.notifyAdmin(tokens.ADMIN_ID,"/_ah/stop is called")
        }
        get("/_ah/start") {
            call.respond("Server is starting!")
            adminBot.notifyAdmin(tokens.ADMIN_ID,"/_ah/start is called")
        }
        post("/${tokens.ADMIN}") {
            val response = call.receiveText()
            botsKeeper.adminBot.processUpdate(response)
            call.respond(HttpStatusCode.OK)
        }
        post("/${tokens.CLIENT}") {
            val response = call.receiveText()
            botsKeeper.clientBot.processUpdate(response)
            call.respond(HttpStatusCode.OK)
        }
    }
}