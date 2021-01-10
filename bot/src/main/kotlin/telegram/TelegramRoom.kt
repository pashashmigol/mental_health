package telegram

import PersonToTest
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.PollAnswerHandlerEnvironment
import mmpi.mockAnswers

object TelegramRoom {
    private const val TAG = "telegram.WorkSpace"
    private val people = mutableMapOf<Long, PersonToTest>()

    fun launchMmpiTest(handler: CommandHandlerEnvironment) {
        println("$TAG: launchMmpiTest();")

        val personId = handler.message.from?.id ?: return
        val personToTest: PersonToTest

        if (people.containsKey(personId)) {
            personToTest = people[personId]!!
        } else {
            people[personId] = PersonToTest(id = personId)
            personToTest = people[personId]!!
        }

        val question = (personToTest.requestFirstQuestion() as NextQuestion)

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
        val personToTest: PersonToTest

        if (people.containsKey(personId)) {
            personToTest = people[personId]!!
        } else {
            people[personId] = PersonToTest(id = personId)
            personToTest = people[personId]!!
        }
        personToTest.requestFirstQuestion()

        mockAnswers.forEach {
            val response = personToTest.submitAnswer(it.option)
            if (response is TestResult) {
                answerWithResult(handler.bot, personId, response)
            }
        }
    }
}