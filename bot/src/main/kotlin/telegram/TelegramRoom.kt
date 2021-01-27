package telegram

import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import lucher.LucherSession
import mmpi.MmpiSession

object TelegramRoom {
    private const val TAG = "telegram.WorkSpace"
    private val sessions = mutableMapOf<Long, TelegramSession>()

    fun launchMmpiTest(handler: CommandHandlerEnvironment) = try {
        println("$TAG: launchMmpiTest();")
        val personId = handler.message.from?.id!!

        sessions.remove(personId)
        sessions[personId] = MmpiSession(personId) { sessions.remove(it.id) }
        sessions[personId]!!.start(handler)

    } catch (e: Exception) {
        sendError(handler.bot, handler.message.from?.id!!, exception = e)
    }

    fun launchLucherTest(handler: CommandHandlerEnvironment) = try {
        println("$TAG: launchLucherTest();")
        val personId = handler.message.from?.id!!

        sessions.remove(personId)
        sessions[personId] = LucherSession(personId) { sessions.remove(it.id) }
        sessions[personId]!!.start(handler)

    } catch (e: Exception) {
        sendError(handler.bot, handler.message.from?.id!!, exception = e)
    }

    fun callbackQuery(env: CallbackQueryHandlerEnvironment) = try {
        val session = sessions[env.callbackQuery.from.id]!!
        session.onCallbackFromUser(env)
    } catch (e: Exception) {
        sendError(
            env.bot, env.update.message!!.from!!.id, exception = e
        )
    }
}



