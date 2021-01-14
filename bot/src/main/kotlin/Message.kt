sealed class Message {
    data class Question(
        val text: String,
        val options: List<String>
    ) : Message()

    data class TestResult(
        val text: String
    ) : Message()
}