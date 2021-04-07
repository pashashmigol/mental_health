package models

data class Question(
    val index: Int,
    val text: String,
    val options: List<Option>
) {
    data class Option(
        val text: String,
        val tag: String
    )
}