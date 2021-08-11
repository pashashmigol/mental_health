package quiz

import models.Question

class DailyQuizData(
    val morningQuestionsClosed: List<Question>,
    val eveningQuestionsClosed: List<Question>,
    val morningQuestionsOpen: List<Question>,
    val eveningQuestionsOpen: List<Question>
)
