package quiz

import models.Question

class DailyQuizData(
    val morningQuestions: List<Question>,
    val eveningQuestions: List<Question>
)
