package quiz

import Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import models.Question
import models.TypeOfTest
import models.User
import storage.CentralDataStorage
import telegram.*
import kotlin.random.Random

private typealias onAnswered = (callback: Callback, messageId: MessageId?) -> Unit

class DailyQuizSession(
    override val user: User,
    override val roomId: RoomId,
    override val chatId: ChatId,
    userConnection: UserConnection,
    override val onEndedCallback: OnEnded
) : TelegramSession<Unit>(
    user,
    chatId,
    roomId,
    TypeOfTest.DailyQuiz,
    userConnection,
    onEndedCallback
) {
    private var onAnswered: onAnswered? = null

    override suspend fun start() {
        val data: DailyQuizData = CentralDataStorage.dailyQuizData

        data.morningQuestions.forEach { ask(it) }
        userConnection.cleanUp(
            chatId = chatId,
            messageIds = state.messageIds
        )

        data.eveningQuestions.forEach { ask(it) }
        userConnection.cleanUp(
            chatId = chatId,
            messageIds = state.messageIds
        )
    }

    private suspend fun ask(question: Question) {
        val channel = Channel<Unit>(0)

        userConnection.sendMessageWithButtons(
            chatId = chatId,
            text = question.text,
            buttons = DailyQuizAnswer.values().map { answer: DailyQuizAnswer ->
                Button(
                    text = answer.title,
                    callback = Callback.DailyQuiz(answer)
                )
            }
        ).let { state.addMessageId(it) }

        onAnswered = { _: Callback, _: MessageId? ->
            channel.offer(Unit)
        }
        channel.receive()
    }

    private val mutex = Mutex()
    override suspend fun onAnswer(callback: Callback, messageId: MessageId?): Result<Unit> {
        mutex.withLock {
            var limit = 1000
            while (onAnswered == null) {
                limit--
                if (limit == 0) {
                    return Result.Error("timeout")
                }
                delay(1)
            }
            onAnswered?.invoke(callback, messageId) ?: Result.Error("onAnswer is null")
        }
        return Result.Success(Unit)
    }
}