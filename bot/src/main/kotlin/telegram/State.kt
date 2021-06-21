package telegram

class State(val sessionId: Long) {
    class Message(val messageId: Long, val data: String)

    private val _messages = mutableListOf<Message>()
    val messages = _messages

    fun add(messageId: Long, data: String) {
        _messages.add(Message(messageId, data))
    }
}