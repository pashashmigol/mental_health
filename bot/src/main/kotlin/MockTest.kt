class MockTest {
    private val questionsProvider = GoogleSheetsQuestionsProvider()

    val questions: List<Question> = questionsProvider.allQuestions
}