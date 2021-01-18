package mmpi

import Gender
import Message
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import telegram.OnEnded
import telegram.TelegramSession

data class MmpiSession(
    override val id: Long,
    val onEndedCallback: OnEnded
) : TelegramSession {

    private var genderPollId: Long = 0L
    private var ongoingProcess: MmpiTestingProcess? = null

    override fun callbackQuery(env: CallbackQueryHandlerEnvironment) {
        if (ongoingProcess == null) {
            val gender = Gender.byValue(env.callbackQuery.data.toInt())
            ongoingProcess = MmpiTestingProcess(gender)

            sendQuestion(
                bot = env.bot,
                userId = id,
                question = ongoingProcess!!.nextQuestion()
            )
        } else {
            ongoingProcess!!.submitAnswer(
                MmpiTestingProcess.Answer.byValue(
                    env.callbackQuery.data.toInt()
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
                onEndedCallback(this)
            }
        }
    }

    override fun start(env: CommandHandlerEnvironment) {
        genderPollId = askGender(bot = env.bot, userId = id, createGenderQuestion())
    }

    private fun sendQuestion(
        bot: Bot,
        userId: Long,
        question: Message.Question
    ): Long {
        val result = bot.editMessageText(
            chatId = userId,
            messageId = genderPollId,
            text = question.text,
            replyMarkup = InlineKeyboardMarkup.create(mmpiButtons(question))
        )
        return result.first!!.body()!!.result!!.messageId
    }

    private fun askGender(
        bot: Bot,
        userId: Long,
        question: Message.Question
    ): Long {
        val result = bot.sendMessage(
            chatId = userId,
            text = question.text,
            replyMarkup = InlineKeyboardMarkup.create(genderButtons(question))
        )
        return result.first!!.body()!!.result!!.messageId
    }

    private fun createGenderQuestion() = Message.Question(
        text = "Выберите себе пол:",
        options = listOf("Мужской", "Женский")
    )

    private fun mmpiButtons(question: Message.Question): List<List<InlineKeyboardButton>> {
        return question.options.map {
            listOf(InlineKeyboardButton.CallbackData(text = it, callbackData = "0"))
        }
    }

    private fun genderButtons(question: Message.Question): List<List<InlineKeyboardButton>> {
        return listOf(question.options.map {
            InlineKeyboardButton.CallbackData(text = it, callbackData = "0")
        })
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
}