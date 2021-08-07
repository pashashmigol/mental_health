package quiz

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import models.Question
import models.TypeOfTest
import models.User
import storage.CentralDataStorage
import telegram.*

private typealias onAnswered = (quizButton: QuizButton, messageId: MessageId?) -> Unit

class DailyQuizSession(
    user: User,
    roomId: RoomId,
    chatId: ChatId,
    userConnection: UserConnection,
    onEndedCallback: OnEnded
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

//        answers.take()
        userConnection.cleanUp(
            chatId = chatId,
            messageIds = state.messageIds
        )

        data.eveningQuestions.forEach { ask(it) }
        userConnection.cleanUp(
            chatId = chatId,
            messageIds = state.messageIds
        )

        onEndedCallback(this)
    }

    private suspend fun ask(question: Question) {
        val channel = Channel<Unit>(0)

        userConnection.sendMessageWithButtons(
            chatId = chatId,
            text = question.text,
            buttons = DailyQuizAnswer.values().map { answer: DailyQuizAnswer ->
                Button(
                    text = answer.title,
                    quizButton = QuizButton.DailyQuiz(answer)
                )
            }
        ).let { state.addMessageId(it) }

        onAnswered = { _: QuizButton, _: MessageId? ->
            channel.offer(Unit)
        }
        channel.receive()
        channel.receiveAsFlow()
    }

//    private val lock = ReentrantLock()



//    override suspend fun onAnswer(callback: Callback, messageId: MessageId?): Result<Unit> {
//
//        val condition = lock.newCondition()
//
//
//        lock.lock()
//        try {
////            var limit = 1000
//            while (onAnswered == null) {
//                condition.awaitNanos(10000L)
//            }
////            while (onAnswered == null) {
////                limit--
////                if (limit == 0) {
////                    return Result.Error("timeout")
////                }
////                delay(1)
////            }
//            onAnswered!!.invoke(callback, messageId) ?: Result.Error("onAnswer is null")
//
//        } finally {
//            lock.unlock()
//        }
//        return Result.Success(Unit)
//    }
}