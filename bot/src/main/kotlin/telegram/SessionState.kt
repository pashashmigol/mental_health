package telegram

import models.TypeOfTest

class SessionState(
    val roomId: Long,
    val sessionId: Long,
    val type: TypeOfTest
) {
    class Message(val messageId: Long, val data: String) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Message

            if (messageId != other.messageId) return false
            if (data != other.data) return false

            return true
        }

        override fun hashCode(): Int {
            var result = messageId.hashCode()
            result = 31 * result + data.hashCode()
            return result
        }
    }

    private val _messages = mutableListOf<Message>()
    val messages: List<Message> = _messages

    fun add(messageId: Long, data: String) {
        _messages.add(Message(messageId, data))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SessionState

        if (roomId != other.roomId) return false
        if (sessionId != other.sessionId) return false
        if (type != other.type) return false
        if (_messages != other._messages) return false

        return true
    }

    override fun hashCode(): Int {
        var result = roomId.hashCode()
        result = 31 * result + sessionId.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + _messages.hashCode()
        return result
    }

}