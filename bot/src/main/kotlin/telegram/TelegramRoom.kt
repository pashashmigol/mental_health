package telegram

import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lucher.telegram.LucherSession
import lucher.telegram.removeMessage
import mmpi.telegram.MmpiSession
import mmpi.telegram.MmpiTestingSession
import models.Type
import storage.CentralDataStorage
import storage.CentralDataStorage.string
import telegram.helpers.ourUser

object TelegramRoom {
    private const val TAG = "telegram.WorkSpace"
    private val sessions = mutableMapOf<Long, TelegramSession>()
    private val scope = GlobalScope


    fun welcomeNewUser(env: CommandHandlerEnvironment) = scope.launch {
        val userId = env.message.from!!.id
        val userName = listOf(env.message.from?.firstName, env.message.from?.lastName)
            .joinToString(separator = " ")

        CentralDataStorage.createUser(userId, userName)

        env.bot.sendMessage(
            chatId = env.message.chat.id,
            text = string("choose_test"),
            replyMarkup = InlineKeyboardMarkup.create(
                listOf(
                    listOf(
                        InlineKeyboardButton.CallbackData(
                            string("lucher"), Type.Lucher.name
                        )
                    ),
                    listOf(
                        InlineKeyboardButton.CallbackData(
                            string("mmpi_566"), Type.Mmpi566.name
                        )
                    ),
                    listOf(
                        InlineKeyboardButton.CallbackData(
                            string("mmpi_377"), Type.Mmpi377.name
                        )
                    )
                )
            )
        )
    }

    fun launchMmpi566Test(env: CommandHandlerEnvironment) = scope.launch {
        try {
            println("$TAG: launchMmpi566Test();")
            val personId = env.message.from?.id!!

            sessions.remove(personId)
            sessions[personId] = MmpiSession(personId, Type.Mmpi566) { sessions.remove(it.id) }

            val user = env.ourUser()
            user?.apply {
                sessions[personId]!!.start(
                    user,
                    env.message.chat.id,
                    BotsKeeper.adminBot,
                    BotsKeeper.clientBot
                )
            }

        } catch (e: Exception) {
            sendError(userId = env.message.from?.id!!, exception = e)
        }
    }

    fun launchMmpi377Test(env: CommandHandlerEnvironment) = scope.launch {
        try {
            println("$TAG: launchMmpi377Test();")
            val personId = env.message.from?.id!!

            sessions.remove(personId)
            sessions[personId] = MmpiSession(personId, Type.Mmpi377) { sessions.remove(it.id) }
            sessions[personId]!!.start(
                env.ourUser()!!,
                env.message.chat.id,
                BotsKeeper.adminBot,
                BotsKeeper.clientBot
            )

        } catch (e: Exception) {
            sendError(
                userId = env.message.from?.id!!,
                exception = e
            )
        }
    }

    fun launchMmpiMockTest(env: CommandHandlerEnvironment) = scope.launch {
        try {
            println("$TAG: launchMmpiTest();")
            val personId = env.message.from?.id!!

            sessions.remove(personId)
            sessions[personId] = MmpiTestingSession(personId) { sessions.remove(it.id) }
            sessions[personId]!!.start(
                env.ourUser()!!,
                env.message.chat.id,
                BotsKeeper.adminBot,
                BotsKeeper.clientBot
            )

        } catch (e: Exception) {
            sendError(
                userId = env.message.from?.id!!,
                exception = e
            )
        }
    }

    fun launchLucherTest(env: CommandHandlerEnvironment) = scope.launch {
        try {
            println("$TAG: launchLucherTest();")
            val personId = env.message.from?.id!!

            sessions.remove(personId)
            sessions[personId] = LucherSession(personId) { sessions.remove(it.id) }
            sessions[personId]!!.start(
                env.ourUser()!!,
                env.message.chat.id,
                BotsKeeper.adminBot,
                BotsKeeper.clientBot
            )

        } catch (e: Exception) {
            sendError(userId = env.message.from?.id!!, exception = e)
        }
    }

    fun callbackQuery(env: CallbackQueryHandlerEnvironment) = scope.launch {
        try {
            val userId = env.callbackQuery.from.id
            val session = sessions[userId]

            if (session != null) {
                session.onCallbackFromUser(env)
            } else {
                launchTest(env)
            }
        } catch (e: Exception) {
            val userId = env.callbackQuery.from.id
            sessions.remove(userId)
            sendError(userId, exception = e)
        }
    }

    private fun launchTest(env: CallbackQueryHandlerEnvironment) {
        val type = Type.valueOf(env.callbackQuery.data)
        val personId = env.callbackQuery.from.id
        val chatId = env.callbackQuery.message!!.chat.id
        val messageId = env.callbackQuery.message!!.messageId

        removeMessage(chatId, env.bot, messageId)

        sessions[personId] = when (type) {
            Type.Mmpi566, Type.Mmpi377 -> MmpiSession(personId, type) { sessions.remove(it.id) }
            Type.Lucher -> LucherSession(personId) { sessions.remove(it.id) }
        }
        sessions[personId]!!.start(
            env.ourUser()!!,
            chatId,
            BotsKeeper.adminBot,
            BotsKeeper.clientBot
        )
    }
}




