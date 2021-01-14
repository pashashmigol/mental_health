package telegram

import PersonBeingTested
import Message
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.PollAnswerHandlerEnvironment
import mmpi.mockAnswers

object TelegramRoom {
    private const val TAG = "telegram.WorkSpace"
    private val people = mutableMapOf<Long, PersonBeingTested>()

    fun launchMmpiTest(handler: CommandHandlerEnvironment) = try {
        println("$TAG: launchMmpiTest();")

        val personId = handler.message.from?.id!!
        val personBeingTested: PersonBeingTested

        if (people.containsKey(personId)) {
            personBeingTested = people[personId]!!
        } else {
            people[personId] = PersonBeingTested(id = personId)
            personBeingTested = people[personId]!!
        }
        val question = (personBeingTested.startMmpiTestAndGetFirstQuestion() as Message.Question)
        answerWithQuestion(handler.bot, personId, question)

    } catch (e: Exception) {
        answerWithError(handler.bot, handler.message.from?.id!!, exception = e)
    }

    fun onAnswer(handler: PollAnswerHandlerEnvironment) = try {
        println("$TAG: onAnswer();")

        val personId = handler.pollAnswer.user.id
        val person = people[personId]!!

        val answerIndex: Int = handler.pollAnswer.optionIds.first()

        when (val response = person.notifyAnswerReceived(answerIndex)) {
            is Message.Question -> {
                answerWithQuestion(handler.bot, personId, response)
            }
            is Message.TestResult -> {
                answerWithResult(handler.bot, personId, response)
            }
        }
    } catch (e: Exception) {
        answerWithError(handler.bot, handler.pollAnswer.user.id, exception = e)
    }

    fun makeMockTest(handler: CommandHandlerEnvironment) = try {
        println("$TAG: makeMockTest();")

        val personId = handler.message.from!!.id
        val personBeingTested: PersonBeingTested

        if (people.containsKey(personId)) {
            personBeingTested = people[personId]!!
        } else {
            people[personId] = PersonBeingTested(id = personId)
            personBeingTested = people[personId]!!
        }
        personBeingTested.startMmpiTestAndGetFirstQuestion()
        personBeingTested.notifyAnswerReceived(0)

        mockAnswers.forEach {
            val response = personBeingTested.notifyAnswerReceived(it.option)
            if (response is Message.TestResult) {
                answerWithResult(handler.bot, personId, response)
            }
        }
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

private fun answerWithQuestion(
    bot: Bot,
    userId: Long,
    question: Message.Question
) {
    bot.sendPoll(
        chatId = userId,
        question = question.text,
        options = question.options.toList(),
        isAnonymous = false
    )
}