package telegram

import PersonBeingTested
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.PollAnswerHandlerEnvironment
import mmpi.mockAnswers

object TelegramRoom {
    private const val TAG = "telegram.WorkSpace"
    private val people = mutableMapOf<Long, PersonBeingTested>()

    fun launchMmpiTest(handler: CommandHandlerEnvironment) {
        println("$TAG: launchMmpiTest();")

        val personId = handler.message.from?.id ?: return
        val personBeingTested: PersonBeingTested

        if (people.containsKey(personId)) {
            personBeingTested = people[personId]!!
        } else {
            people[personId] = PersonBeingTested(id = personId)
            personBeingTested = people[personId]!!
        }

        val question = (personBeingTested.requestFirstQuestion() as NextQuestion)

        answerWithQuestion(handler.bot, personId, question)
    }

    fun onAnswer(handler: PollAnswerHandlerEnvironment) {
        println("$TAG: onAnswer();")

        val personId = handler.pollAnswer.user.id
        val person = people[personId]!!

        val answerIndex: Int = handler.pollAnswer.optionIds.first()

        when (val response = person.submitAnswer(answerIndex)) {
            is NextQuestion -> {
                answerWithQuestion(handler.bot, personId, response)
            }
            is TestResult -> {
                answerWithResult(handler.bot, personId, response)
            }
        }
    }

    private fun answerWithResult(
        bot: Bot,
        userId: Long,
        result: TestResult
    ) {
        println("$TAG: answerWithResult();")
        bot.sendMessage(
            chatId = userId,
            text = result.text()
        )
    }

    private fun answerWithQuestion(
        bot: Bot,
        userId: Long,
        question: NextQuestion
    ) {
        println("$TAG: answerWithQuestion();")
        bot.sendPoll(
            chatId = userId,
            question = question.question.text,
            options = question.question.options.toList(),
            isAnonymous = false
        )
    }

    fun makeMockTest(handler: CommandHandlerEnvironment) {
        println("$TAG: makeMockTest();")

        val personId = handler.message.from?.id ?: return
        val personBeingTested: PersonBeingTested

        if (people.containsKey(personId)) {
            personBeingTested = people[personId]!!
        } else {
            people[personId] = PersonBeingTested(id = personId)
            personBeingTested = people[personId]!!
        }
        personBeingTested.requestFirstQuestion()

        mockAnswers.forEach {
            val response = personBeingTested.submitAnswer(it.option)
            if (response is TestResult) {
                answerWithResult(handler.bot, personId, response)
            }
        }
    }
}