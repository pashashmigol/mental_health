package telegram

import models.TypeOfTest

class SessionState(
    val roomId: Long,
    val sessionId: Long,
    val type: TypeOfTest,
    val userId: Long,
    val chatId: Long
) {
    val answers: List<Callback> = mutableListOf()
    fun addAnswer(callback: Callback) {
        (answers as MutableList).add(callback)
    }

    val messageIds: List<MessageId> = mutableListOf()
    fun addMessageId(messageId: MessageId?) {
        messageId?.let { (messageIds as MutableList).add(messageId) }
    }

    fun addMessageIds(messageIds: Collection<MessageId>?) {
        messageIds?.let { (this.messageIds as MutableList).addAll(messageIds) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SessionState

        if (roomId != other.roomId) return false
        if (sessionId != other.sessionId) return false
        if (type != other.type) return false
        if (userId != other.userId) return false
        if (chatId != other.chatId) return false
        if (answers != other.answers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = roomId.hashCode()
        result = 31 * result + sessionId.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + chatId.hashCode()
        result = 31 * result + answers.hashCode()
        return result
    }

    override fun toString(): String {
        return "SessionState(roomId=$roomId, sessionId=$sessionId, type=$type, userId=$userId, chatId=$chatId, answers=$answers)"
    }


}