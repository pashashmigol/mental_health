package mmpi.telegram

import Gender
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import io.ktor.util.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.launch
import mmpi.MmpiProcess
import mmpi.askGender
import mmpi.createGenderQuestion
import mmpi.report.generateReport
import mmpi.sendQuestion
import storage.CentralDataStorage
import telegram.OnEnded
import telegram.TelegramSession
import telegram.helpers.showResult
import telegram.sendError


typealias OnAnswerReceived = (answer: String) -> Unit

@KtorExperimentalAPI
open class MmpiSession(
    override val id: Long,
    open val onEndedCallback: OnEnded
) : TelegramSession {
    companion object {
        val scope = GlobalScope
    }

    internal var onAnswer: OnAnswerReceived? = null

    override fun start(env: CommandHandlerEnvironment) {
        val userId = env.message.from!!.id

        val handler = CoroutineExceptionHandler { _, exception ->
            sendError(env.bot, userId, "MmpiSession error", exception)
        }
        scope.launch(handler) { executeTesting(env) }
    }

    private suspend fun executeTesting(env: CommandHandlerEnvironment) {
        //using channel to wait until all colors are chosen
        val gChannel = Channel<Gender>(RENDEZVOUS)

        val messageId = askGender(
            bot = env.bot,
            userId = id,
            createGenderQuestion()
        )
        onAnswer = { answer: String ->
            val gender = Gender.byValue(answer.toInt())
            gChannel.offer(gender)
        }

        val gender = gChannel.receive()
        val ongoingProcess = MmpiProcess(gender)

        onAnswer = { answer: String ->
            println("executeTesting.onAnswer($answer);")

            ongoingProcess.submitAnswer(
                MmpiProcess.Answer.byValue(answer.toInt())
            )
            if (ongoingProcess.hasNextQuestion()) {
                sendNextQuestion(env, messageId, ongoingProcess)
            } else {
                env.bot.deleteMessage(id, messageId)
                finishTesting(ongoingProcess, env)
            }
        }
        sendNextQuestion(env, messageId, ongoingProcess)
    }

    private fun finishTesting(
        ongoingProcess: MmpiProcess,
        env: CommandHandlerEnvironment
    ) {
        val userName = "${env.message.from!!.firstName} ${env.message.from!!.lastName}"
        val result = ongoingProcess.calculateResult()

        val report = generateReport(
            userId = userName,
            questions = ongoingProcess.questions,
            answers = ongoingProcess.answers,
            result = result
        )
        val resultFolder = CentralDataStorage.saveMmpi(
            userId = userName,
            report = report
        )
        showResult(
            bot = env.bot,
            userId = id,
            link = resultFolder
        )
        onEndedCallback(this)
    }

    internal open fun sendNextQuestion(
        env: CommandHandlerEnvironment,
        messageId: Long,
        ongoingProcess: MmpiProcess
    ) {
        sendQuestion(
            bot = env.bot,
            userId = id,
            messageId = messageId,
            question = ongoingProcess.nextQuestion()
        )
    }

    override fun onCallbackFromUser(env: CallbackQueryHandlerEnvironment) {
        onAnswer?.invoke(env.callbackQuery.data)
    }
}