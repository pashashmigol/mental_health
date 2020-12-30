package com.github.kotlintelegrambot.echo

class MockTest {
    private val options = listOf("yes", "no", "whoy wnos")
    val questions: List<Question> = listOf(
        Question(text = "question 1", options = options),
        Question(text = "question 2", options = options),
        Question(text = "question 3", options = options),
        Question(text = "question 4", options = options),
        Question(text = "question 5", options = options)
    )
}