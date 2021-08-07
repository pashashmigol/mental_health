package telegram

import models.TypeOfTest
import storage.CentralDataStorage

class SessionState(
    val roomId: Long,
    val sessionId: Long,
    val type: TypeOfTest,
    val userId: Long,
    val chatId: Long
) {
    val answers: List<QuizButton> = mutableListOf()
    fun addAnswer(quizButton: QuizButton) {
        (answers as MutableList).add(quizButton)
    }

    suspend fun addToStorage(){
        CentralDataStorage.usersStorage.addSession(this)
    }

    suspend fun saveAnswer(quizButton: QuizButton) {
        CentralDataStorage.usersStorage.addAnswer(
            sessionId = this.sessionId,
            quizButton = quizButton,
            index = answers.size
        )
        (answers as MutableList).add(quizButton)
    }

    val messageIds: List<MessageId> = mutableListOf()
    fun addMessageId(messageId: MessageId?) {
        messageId
            ?.takeIf { messageId != NOT_SENT }
            ?.let {
                (messageIds as MutableList).add(messageId)
            }
    }

    suspend fun saveMessageId(messageId: MessageId?) {
        messageId
            ?.takeIf { messageId != NOT_SENT }
            ?.let {
                (messageIds as MutableList).add(messageId)

                CentralDataStorage.usersStorage.addMessageId(
                    sessionId = this.sessionId,
                    messageId = messageId,
                    index = answers.size
                )
            }
    }

    fun addMessageIds(messageIds: Collection<MessageId>?) {
        messageIds
            ?.filter { it != NOT_SENT }
            ?.let { (this.messageIds as MutableList).addAll(it) }
    }

    override fun toString(): String {
        return "SessionState(roomId=$roomId, sessionId=$sessionId, type=$type, userId=$userId, chatId=$chatId, answers=$answers)"
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
        if (messageIds != other.messageIds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = roomId.hashCode()
        result = 31 * result + sessionId.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + chatId.hashCode()
        result = 31 * result + answers.hashCode()
        result = 31 * result + messageIds.hashCode()
        return result
    }
}