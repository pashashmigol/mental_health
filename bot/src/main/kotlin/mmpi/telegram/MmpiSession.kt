package mmpi.telegram

import Gender
import Settings.ADMIN_ID
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
import telegram.UserConnection
import telegram.helpers.showResult
import telegram.sendError


typealias OnAnswerReceived = (answer: String) -> Unit

open class MmpiSession(
    override val id: Long,
    private val type: Type,
    open val clientConnection: UserConnection,
    open val adminConnection: UserConnection,
    open val onEndedCallback: OnEnded
) : TelegramSession {
    companion object {
        val scope = GlobalScope
    }

    internal var onAnswer: OnAnswerReceived? = null

    override fun start(user: User, chatId: Long) {
        val handler = CoroutineExceptionHandler { _, exception ->
            sendError("MmpiSession error", exception)
        }
        scope.launch(handler) { executeTesting(user) }
    }

    private suspend fun executeTesting(user: User) {
        //using channel to wait until all colors are chosen
        val gChannel = Channel<Gender>(RENDEZVOUS)

        val messageId = askGender(
            userId = id,
            createGenderQuestion(),
            connection = clientConnection
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
                sendNextQuestion(messageId, ongoingProcess, clientConnection)
            } else {
                clientConnection.removeMessage(id, messageId)
                finishTesting(ongoingProcess, user, clientConnection, adminConnection)
            }
        }
        sendNextQuestion(messageId, ongoingProcess, clientConnection)
    }

    private fun finishTesting(
        ongoingProcess: MmpiProcess,
        user: User,
        clientConnection: UserConnection,
        adminConnection: UserConnection,
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
            user = user,
            adminId = ADMIN_ID,
            resultLink = resultFolder,
            clientConnection,
            adminConnection
        )
        onEndedCallback(this)
    }

    internal open fun sendNextQuestion(
        messageId: Long,
        ongoingProcess: MmpiProcess,
        userConnection: UserConnection
    ) {
        userConnection.updateMessage(
            chatId = id,
            messageId = messageId,
            text = ongoingProcess.nextQuestion().text,
            buttons = mmpiButtons(ongoingProcess.nextQuestion())
        )
    }

    override fun onCallbackFromUser(messageId: Long, data: String) {
        onAnswer?.invoke(data)
    }
}