package telegram

import Gender
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.PollAnswerHandlerEnvironment
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import mmpi.MmpiTestingProcess

typealias OnEnded = (TelegramSession) -> Unit

interface TelegramSession {
    val id: Long
    fun pollAnswer(env: PollAnswerHandlerEnvironment)
    fun callbackQuery()
    fun start(env: CommandHandlerEnvironment)
}

data class MmpiSession(
    override val id: Long,
    val callback: OnEnded
) : TelegramSession {

    var genderPollId = ""
    var ongoingProcess: MmpiTestingProcess? = null

    override fun pollAnswer(env: PollAnswerHandlerEnvironment) {
        if (env.pollAnswer.pollId == genderPollId) {
            val gender = Gender.valueOf(env.pollAnswer.optionIds[0].toString())
            ongoingProcess = MmpiTestingProcess(gender)
        } else {
            ongoingProcess!!.submitAnswer(
                MmpiTestingProcess.Answer.byValue(
                    env.pollAnswer.optionIds.first()
                )
            )
            if (ongoingProcess!!.hasNextQuestion()) {
                sendQuestion(
                    bot = env.bot,
                    userId = id,
                    question = ongoingProcess!!.nextQuestion()
                )
            } else {
                sendResult(
                    bot = env.bot,
                    userId = id,
                    result = Message.TestResult(ongoingProcess!!.calculateResult().format())
                )
            }
        }
    }

    override fun callbackQuery() {
        TODO("Not yet implemented")
    }

    override fun start(env: CommandHandlerEnvironment) {
        genderPollId = sendQuestion(
            bot = env.bot,
            userId = id,
            question = createGenderQuestion()
        )
    }
}

data class LucherSession(
    override val id: Long,
    val callback: OnEnded
) : TelegramSession {
    override fun pollAnswer(env: PollAnswerHandlerEnvironment) {
        TODO("Not yet implemented")
    }

    override fun callbackQuery() {
        TODO("Not yet implemented")
    }

    override fun start(env: CommandHandlerEnvironment) {
        TODO("Not yet implemented")
    }
}

private fun createGenderQuestion() = Message.Question(
    text = "Выберите себе пол:",
    options = listOf("Мужской", "Женский")
)

private fun sendQuestion(
    bot: Bot,
    userId: Long,
    question: Message.Question
): String {
    val poll = bot.sendPoll(
        chatId = userId,
        question = question.text,
        options = question.options.toList(),
        isAnonymous = false,
        replyMarkup = InlineKeyboardMarkup.create(replyOptions(question))
    )
    return poll.first!!.body()!!.result!!.poll!!.id.toString()
}

private fun replyOptions(question: Message.Question): List<InlineKeyboardButton> {
    return listOf(
        InlineKeyboardButton.CallbackData(text = "1", callbackData = "1"),
        InlineKeyboardButton.CallbackData(text = "2", callbackData = "2")
    )
}

private fun sendResult(
    bot: Bot,
    userId: Long,
    result: Message.TestResult
) {
    bot.sendMessage(
        chatId = userId,
        text = result.text
    )
}