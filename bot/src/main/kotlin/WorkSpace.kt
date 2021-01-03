import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.PollAnswerHandlerEnvironment

object WorkSpace {

    private val people = mutableMapOf<Long, PersonBeingTested>()


    fun launchMockTest(handler: CommandHandlerEnvironment) {
        val personId = handler.message.from?.id ?: return

        val personBeingTested: PersonBeingTested
        if (people.containsKey(personId)) {
            personBeingTested = people[personId]!!
        } else {
            people[personId] = PersonBeingTested(id = personId)
            personBeingTested = people[personId]!!
        }

        val firstQuestion = personBeingTested.startMockTest()

        handler.bot.sendPoll(
            chatId = handler.message.chat.id,
            question = firstQuestion.text,
            options = firstQuestion.options,
            isAnonymous = false
        )
    }

    fun onAnswer(handler: PollAnswerHandlerEnvironment) {
        val personId = handler.pollAnswer.user.id
        val personBeingTested = people[personId]!!

        val answerIndex: Int = handler.pollAnswer.optionIds.first()
        val nextQuestion = personBeingTested.postAnswer(answerIndex)

        handler.bot.sendPoll(
            chatId = handler.pollAnswer.user.id,
            question = nextQuestion.text,
            options = nextQuestion.options,
            isAnonymous = false
        )
    }
}