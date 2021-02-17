package mmpi

import Gender
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.launch
import storage.CentralDataStorage
import telegram.OnEnded
import telegram.TelegramSession
import telegram.sendError


typealias OnAnswerReceived = (answer: String) -> Unit

data class MmpiSession(
    override val id: Long,
    val onEndedCallback: OnEnded
) : TelegramSession {
    companion object {
        val scope = GlobalScope
    }

    private var onAnswer: OnAnswerReceived? = null

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

        val messageId = askGender(bot = env.bot, userId = id, createGenderQuestion())
        onAnswer = { answer: String ->
            val gender = Gender.byValue(answer.toInt())
            gChannel.offer(gender)
        }

        val gender = gChannel.receive()
        val ongoingProcess = MmpiTestingProcess(gender)
        sendNextQuestion(env, messageId, ongoingProcess)

        onAnswer = { answer: String ->
            ongoingProcess.submitAnswer(
                MmpiTestingProcess.Answer.byValue(answer.toInt())
            )
            if (ongoingProcess.hasNextQuestion()) {
                sendNextQuestion(env, messageId, ongoingProcess)
            } else {
                finishTesting(ongoingProcess, env)
            }
        }
    }

    private fun finishTesting(
        ongoingProcess: MmpiTestingProcess,
        env: CommandHandlerEnvironment
    ) {
        val userName = "${env.message.from!!.firstName} ${env.message.from!!.lastName}"
        val result = ongoingProcess.calculateResult().format()

        CentralDataStorage.saveMmpi(
            userId = userName,
            questions = ongoingProcess.questions,
            answers = ongoingProcess.answers,
            result = result
        )
        sendResult(
            bot = env.bot,
            userId = id,
            result = result
        )
        onEndedCallback(this)
    }

    private fun sendNextQuestion(
        env: CommandHandlerEnvironment,
        messageId: Long,
        ongoingProcess: MmpiTestingProcess
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