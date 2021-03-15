package telegram

import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import lucher.telegram.LucherSession
import mmpi.Type
import mmpi.telegram.MmpiSession
import mmpi.telegram.MmpiTestingSession

object TelegramRoom {
    private const val TAG = "telegram.WorkSpace"
    private val sessions = mutableMapOf<Long, TelegramSession>()

    fun launchMmpi566Test(handler: CommandHandlerEnvironment) = try {
        println("$TAG: launchMmpi566Test();")
        val personId = handler.message.from?.id!!

        sessions.remove(personId)
        sessions[personId] = MmpiSession(personId, Type.Mmpi566) { sessions.remove(it.id) }
        sessions[personId]!!.start(handler)

    } catch (e: Exception) {
        sendError(handler.bot, handler.message.from?.id!!, exception = e)
    }

    fun launchMmpi377Test(handler: CommandHandlerEnvironment) = try {
        println("$TAG: launchMmpi377Test();")
        val personId = handler.message.from?.id!!

        sessions.remove(personId)
        sessions[personId] = MmpiSession(personId, Type.Mmpi377) { sessions.remove(it.id) }
        sessions[personId]!!.start(handler)

    } catch (e: Exception) {
        sendError(handler.bot, handler.message.from?.id!!, exception = e)
    }

    fun launchMmpiMockTest(handler: CommandHandlerEnvironment) = try {
        println("$TAG: launchMmpiTest();")
        val personId = handler.message.from?.id!!

        sessions.remove(personId)
        sessions[personId] = MmpiTestingSession(personId) { sessions.remove(it.id) }
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



