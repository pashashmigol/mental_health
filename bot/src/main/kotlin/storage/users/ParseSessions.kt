package storage.users

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import lucher.LucherColor
import mmpi.MmpiProcess
import models.TypeOfTest
import quiz.DailyQuizOptions
import telegram.*

@Suppress("UNCHECKED_CAST")
fun parseSessions(snapshot: DataSnapshot?): List<SessionState> {

    val typeIndicator: GenericTypeIndicator<HashMap<String, Any>> =
        object : GenericTypeIndicator<HashMap<String, Any>>() {}

    return snapshot?.children?.map { dataSnapshot ->
        val sessionMap = dataSnapshot.getValue(typeIndicator)

        val sessionState = SessionState(
            userId = sessionMap["userId"] as UserId,
            roomId = sessionMap["roomId"] as RoomId,
            chatId = sessionMap["chatId"] as ChatId,
            sessionId = sessionMap["sessionId"] as SessionId,
            type = TypeOfTest.valueOf(sessionMap["type"] as String)
        )

        (sessionMap["messageIds"] as? ArrayList<Long>)
            ?.apply { sessionState.addMessageIds(this) }

        (sessionMap["answers"] as? ArrayList<HashMap<String, Any>>)
            ?.forEach {
                val type = UserAnswer.Type.valueOf(it["type"] as String)
                val index = (it["index"] as? Long)?.toInt()
                val answer = it["answer"] as String

                val callback = when (type) {
                    UserAnswer.Type.Gender -> {
                        UserAnswer.GenderAnswer(Gender.valueOf(answer))
                    }
                    UserAnswer.Type.Mmpi -> {
                        UserAnswer.Mmpi(index!!, MmpiProcess.Answer.valueOf(answer))
                    }
                    UserAnswer.Type.Lucher -> {
                        UserAnswer.Lucher(LucherColor.valueOf(answer))
                    }
                    UserAnswer.Type.NewTestRequest -> {
                        UserAnswer.NewTest(TypeOfTest.valueOf(answer))
                    }
                    UserAnswer.Type.DailyQuiz -> {
                        UserAnswer.DailyQuiz(DailyQuizOptions.valueOf(answer))
                    }
                    UserAnswer.Type.Skip -> {
                        UserAnswer.Skip()
                    }
                    UserAnswer.Type.Text -> {
                        UserAnswer.Text(answer)
                    }
                    UserAnswer.Type.Switch -> {
                        UserAnswer.Switch(answer.toBoolean())
                    }
                }
                sessionState.addAnswer(callback)
            }
        sessionState
    } ?: emptyList()
}