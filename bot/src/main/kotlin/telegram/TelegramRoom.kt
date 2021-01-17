package telegram

import Message
import PersonBeingTested
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.PollAnswerHandlerEnvironment
import mmpi.mockAnswers

object TelegramRoom {
    private const val TAG = "telegram.WorkSpace"
    private val people = mutableMapOf<Long, PersonBeingTested>()
    private val sessions = mutableMapOf<Long, TelegramSession>()

    fun launchMmpiTest(handler: CommandHandlerEnvironment) = try {
        println("$TAG: launchMmpiTest();")

        val personId = handler.message.from?.id!!
//        val personBeingTested: PersonBeingTested
//
//        if (people.containsKey(personId)) {
//            personBeingTested = people[personId]!!
//        } else {
//            people[personId] = PersonBeingTested(id = personId)
//            personBeingTested = people[personId]!!
//        }
//        val question = (personBeingTested.startMmpiTestAndGetFirstQuestion() as Message.Question)
//        answerWithQuestion(handler.bot, personId, question)

        sessions.remove(personId)
        sessions[personId] = MmpiSession(personId) { sessions.remove(it.id) }
        sessions[personId]!!.start(handler)

    } catch (e: Exception) {
        answerWithError(handler.bot, handler.message.from?.id!!, exception = e)
    }

    fun launchLucherTest(handler: CommandHandlerEnvironment) = try {
        println("$TAG: launchLucherTest();")
        val personId = handler.message.from?.id!!

        sessions.remove(personId)
        sessions[personId] = LucherSession(personId) { sessions.remove(it.id) }
        sessions[personId]!!.start(handler)

    } catch (e: Exception) {
        answerWithError(handler.bot, handler.message.from?.id!!, exception = e)
    }

    fun callbackQuery(handler: CallbackQueryHandlerEnvironment) = try {
        val session = sessions[handler.callbackQuery.from.id]!!
        session.callbackQuery(handler)
    } catch (e: Exception) {
        answerWithError(
            handler.bot, handler.update.message!!.from!!.id, exception = e
        )
    }

    fun pollAnswer(handler: PollAnswerHandlerEnvironment) = try {
        println("$TAG: onAnswer();")

//        val personId = handler.pollAnswer.user.id
//        val person = people[personId]!!
//
//        val answerIndex: Int = handler.pollAnswer.optionIds.first()
//
//        when (val response = person.notifyAnswerReceived(answerIndex)) {
//            is Message.Question -> {
//                answerWithQuestion(handler.bot, personId, response)
//            }
//            is Message.TestResult -> {
//                answerWithResult(handler.bot, personId, response)
//            }
//        }

        val session = sessions[handler.pollAnswer.user.id]!!
        session.pollAnswer(handler)


    } catch (e: Exception) {
        answerWithError(handler.bot, handler.pollAnswer.user.id, exception = e)
    }

    fun makeMockTest(handler: CommandHandlerEnvironment) = try {
        println("$TAG: makeMockTest();")

//        val personId = handler.message.from!!.id
//        val personBeingTested: PersonBeingTested
//
//        if (people.containsKey(personId)) {
//            personBeingTested = people[personId]!!
//        } else {
//            people[personId] = PersonBeingTested(id = personId)
//            personBeingTested = people[personId]!!
//        }
//        personBeingTested.startMmpiTestAndGetFirstQuestion()
//        personBeingTested.notifyAnswerReceived(0)
//
//        mockAnswers.forEach {
//            val response = personBeingTested.notifyAnswerReceived(it.option)
//            if (response is Message.TestResult) {
//                answerWithResult(handler.bot, personId, response)
//            }
//        }
    } catch (e: java.lang.Exception) {
        answerWithError(handler.bot, handler.message.from!!.id, exception = e)
    }
}


private fun answerWithError(
    bot: Bot,
    userId: Long,
    message: String? = null,
    exception: java.lang.Exception? = null
) {
    bot.sendMessage(
        chatId = userId,
        text = message + "\n\n" + exception?.message +
                "\n\n" + exception?.stackTrace.contentToString()
    )
}

private fun answerWithResult(
    bot: Bot,
    userId: Long,
    result: Message.TestResult
) {
    bot.sendMessage(
        chatId = userId,
        text = result.text
    )
}



