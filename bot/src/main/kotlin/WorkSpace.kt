import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.PollAnswerHandlerEnvironment

object WorkSpace {
    const val TAG = "WorkSpace"
    private val people = mutableMapOf<Long, Person>()

    fun launchMmpiTest(handler: CommandHandlerEnvironment) {
        println("$TAG: launchMmpiTest();")

        val personId = handler.message.from?.id ?: return
        val person: Person

        if (people.containsKey(personId)) {
            person = people[personId]!!
        } else {
            people[personId] = Person(id = personId)
            person = people[personId]!!
        }

        val question = (person.requestFirstQuestion()
                as Person.Response.NextQuestion)

        answerWithQuestion(handler.bot, personId, question)
    }

    fun onAnswer(handler: PollAnswerHandlerEnvironment) {
        println("$TAG: onAnswer();")

        val personId = handler.pollAnswer.user.id
        val person = people[personId]!!

        val answerIndex: Int = handler.pollAnswer.optionIds.first()

        when (val response = person.submitAnswer(answerIndex)) {
            is Person.Response.NextQuestion -> {
                answerWithQuestion(handler.bot, personId, response)
            }
            is Person.Response.TestResult -> {
                answerWithResult(handler.bot, personId, response)
            }
        }
    }

    private fun answerWithResult(
        bot: Bot,
        userId: Long,
        result: Person.Response.TestResult
    ) {
        println("$TAG: answerWithResult();")
        bot.sendMessage(
            chatId = userId,
            text = result.description
        )
    }

    private fun answerWithQuestion(
        bot: Bot,
        userId: Long,
        question: Person.Response.NextQuestion
    ) {
        println("$TAG: answerWithQuestion();")
        bot.sendPoll(
            chatId = userId,
            question = question.question.text,
            options = question.question.options.toList(),
            isAnonymous = false
        )
    }
}