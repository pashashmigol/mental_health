package telegram

import models.User
import Result
import models.TypeOfTest


typealias OnEnded = (TelegramSession<Any>) -> Unit

typealias MessageId = Long
typealias RoomId = Long
typealias ChatId = Long
typealias UserId = Long
typealias SessionId = Long

abstract class TelegramSession<out T>(
    open val user: User,
    open val roomId: RoomId,
    open val chatId: ChatId,
    open val type: TypeOfTest,
    open val userConnection: UserConnection,
    open val onEndedCallback: OnEnded
) {
    val sessionId by lazy { user.id }

    val state by lazy {
        SessionState(
            userId = user.id,
            sessionId = user.id,
            chatId = chatId,
            roomId = roomId,
            type = type,
        )
    }

    abstract suspend fun start()

//    suspend fun sendAnswer(messageId: Long, data: String): Result<T> {
//        state.addAnswer(messageId, data)
//        return onAnswer(messageId, data)
//    }

    suspend fun sendAnswer(callback: Callback, messageId: MessageId? = null): Result<T> {
        state.addAnswer(callback)
        return onAnswer(callback, messageId)
    }

    abstract suspend fun onAnswer(callback: Callback, messageId: MessageId?): Result<T>

    open suspend fun applyState(state: SessionState) {
        userConnection.pause()
        start()
        state.answers.forEach {
            sendAnswer(it)
        }
        userConnection.resume()
    }
}
