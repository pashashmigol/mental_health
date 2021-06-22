package telegram

import models.TypeOfTest

class SessionState(
    val sessionId: Long,
    val type: TypeOfTest
) {
    class Message(val messageId: Long, val data: String)

    private val _messages = mutableListOf<Message>()
    val messages = _messages

    fun add(messageId: Long, data: String) {
        _messages.add(Message(messageId, data))
    }
}