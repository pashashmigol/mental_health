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
import telegram.notifyAdmin

typealias OnAnswerReceived = (messageId: Long, answer: String) -> Unit

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
            notifyAdmin("MmpiSession error", exception)
        }
        scope.launch(handler) { executeTesting(user) }
    }

    private suspend fun executeTesting(user: User) {
        //using channel to wait until all colors are chosen
        val gChannel = Channel<Gender>(RENDEZVOUS)

        askGender(
            userId = id,
            question = createGenderQuestion(),
            connection = clientConnection
        )
        onAnswer = { messageId: Long, answer: String ->
            clientConnection.highlightAnswer(messageId, answer)

            val gender = Gender.valueOf(answer)
            gChannel.offer(gender)
        }

        val gender = gChannel.receive()
        val ongoingProcess = MmpiProcess(gender, type)

        val mesIdToIndex = mutableMapOf<Long, Int>()

        onAnswer = { mesId: Long, answer: String ->
            println("executeTesting.onAnswer($answer);")

            clientConnection.highlightAnswer(mesId, answer)

            if (ongoingProcess.hasNextQuestion()) {
                if (ongoingProcess.isItLastAskedQuestion(mesIdToIndex[mesId])) {
                    sendNextQuestion(ongoingProcess, clientConnection).apply {
                        mesIdToIndex[first] = second
                    }
                }
            } else {
                clientConnection.cleanUp()
                finishTesting(ongoingProcess, user, clientConnection, adminConnection)
            }
            ongoingProcess.submitAnswer(
                mesIdToIndex[mesId]!!, MmpiProcess.Answer.valueOf(answer)
            )
        }
        sendNextQuestion(ongoingProcess, clientConnection).apply {
            mesIdToIndex[first] = second
        }
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
            clientConnection = clientConnection,
            adminConnection = adminConnection
        )
        onEndedCallback(this)
    }

    internal open fun sendNextQuestion(
        ongoingProcess: MmpiProcess,
        userConnection: UserConnection
    ): Pair<Long, Int> {
        val question = ongoingProcess.nextQuestion()
        val messageId = userConnection.sendMessageWithButtons(
            chatId = id,
            text = question.text,
            buttons = buttons(question)
        )
        return Pair(messageId, question.index)
    }

    override fun onCallbackFromUser(messageId: Long, data: String) {
        onAnswer?.invoke(messageId, data)
    }
}