package mmpi.telegram

import Gender
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.launch
import mmpi.*
import mmpi.report.generateReport
import models.Type
import models.User
import storage.CentralDataStorage
import telegram.OnEnded
import telegram.TelegramSession
import telegram.helpers.showResult
import telegram.sendError


typealias OnAnswerReceived = (answer: String) -> Unit

open class MmpiSession(
    override val id: Long,
    private val type: Type,
    open val onEndedCallback: OnEnded
) : TelegramSession {
    companion object {
        val scope = GlobalScope
    }

    internal var onAnswer: OnAnswerReceived? = null

    override fun start(user: User, chatId: Long, bot: Bot) {
        val handler = CoroutineExceptionHandler { _, exception ->
            sendError(bot, user.id, "MmpiSession error", exception)
        }
        scope.launch(handler) { executeTesting(user, bot) }
    }

    private suspend fun executeTesting(user: User, bot: Bot) {
        //using channel to wait until all colors are chosen
        val gChannel = Channel<Gender>(RENDEZVOUS)

        val messageId = askGender(
            bot = bot,
            userId = id,
            createGenderQuestion()
        )
        onAnswer = { answer: String ->
            val gender = Gender.byValue(answer.toInt())
            gChannel.offer(gender)
        }

        val gender = gChannel.receive()
        val ongoingProcess = MmpiProcess(gender, type)

        onAnswer = { answer: String ->
            println("executeTesting.onAnswer($answer);")

            ongoingProcess.submitAnswer(
                MmpiProcess.Answer.byValue(answer.toInt())
            )
            if (ongoingProcess.hasNextQuestion()) {
                sendNextQuestion(bot, messageId, ongoingProcess)
            } else {
                bot.deleteMessage(id, messageId)
                finishTesting(ongoingProcess, user, bot)
            }
        }
        sendNextQuestion(bot, messageId, ongoingProcess)
    }

    private fun finishTesting(
        ongoingProcess: MmpiProcess,
        user: User,
        bot: Bot
    ) {
        val result = ongoingProcess.calculateResult()

        val report = generateReport(
            userId = user.name,
            questions = ongoingProcess.questions,
            answers = ongoingProcess.answers,
            result = result
        )
        val resultFolder = CentralDataStorage.reports.saveMmpi(
            userId = user.name,
            report = report,
            type = type
        )
        showResult(
            bot = bot,
            userId = id,
            link = resultFolder
        )
        onEndedCallback(this)
    }

    internal open fun sendNextQuestion(
        bot: Bot,
        messageId: Long,
        ongoingProcess: MmpiProcess
    ) {
        sendQuestion(
            bot = bot,
            userId = id,
            messageId = messageId,
            question = ongoingProcess.nextQuestion()
        )
    }

    override fun onCallbackFromUser(env: CallbackQueryHandlerEnvironment) {
        onAnswer?.invoke(env.callbackQuery.data)
    }
}