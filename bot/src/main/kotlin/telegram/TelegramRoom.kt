package telegram

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import lucher.telegram.LucherSession
import mmpi.telegram.MmpiSession
import models.TypeOfTest.*
import storage.CentralDataStorage
import storage.CentralDataStorage.string

import Result
import io.ktor.util.*
import io.ktor.util.collections.*
import kotlinx.coroutines.runBlocking
import models.TypeOfTest
import quiz.DailyQuizSession


private const val TAG = "telegram.WorkSpace"

@InternalAPI
class TelegramRoom(
    val roomId: Long,
    private val userConnection: UserConnection,
) {
    internal val sessions = ConcurrentMap<Long, TelegramSession<*>>()
    private val scope = GlobalScope

    fun restoreState() = runBlocking {
        val storedSessionStates = CentralDataStorage.usersStorage.takeAllSessions()

        val storedSessions = (storedSessionStates as Result.Success).data
            .filter {
                roomId == it.roomId
            }
            .map { sessionState ->
                val session = restoreSession(sessionState)
                Pair(session.sessionId, session)
            }
        sessions.putAll(storedSessions)
    }

    private suspend fun restoreSession(sessionState: SessionState): TelegramSession<Any> {
        val userId = sessionState.userId
        val user = CentralDataStorage.usersStorage.getUser(userId)!!

        val session = when (sessionState.type) {
            Mmpi566, Mmpi377 -> MmpiSession(
                user = user,
                roomId = roomId,
                chatId = sessionState.chatId,
                type = sessionState.type,
                userConnection = userConnection,
                onEndedCallback = { removeSession(it.sessionId) }
            )
            Lucher -> LucherSession(
                user = user,
                roomId = roomId,
                chatId = sessionState.chatId,
                userConnection = userConnection,
                onEndedCallback = { removeSession(it.sessionId) }
            )
            DailyQuiz -> DailyQuizSession(
                user = user,
                roomId = roomId,
                chatId = sessionState.chatId,
                userConnection = userConnection,
                onEndedCallback = { removeSession(it.sessionId) }
            )
        }
        session.applyState(sessionState)
        return session
    }

    fun welcomeUser(
        chatInfo: ChatInfo,
        userConnection: UserConnection
    ): Job {
        return scope.launch {
            val userId = chatInfo.userId

            if (CentralDataStorage.usersStorage.hasUserWithId(userId)) {
                val user = CentralDataStorage.usersStorage.getUser(userId)

                userConnection.notifyAdmin(
                    "user already exists: $user"
                )
            } else {
                CentralDataStorage.createUser(userId, chatInfo.userName)
                val user = CentralDataStorage.usersStorage.getUser(userId)
                assert(user != null)

                userConnection.notifyAdmin(
                    "user created: $user"
                )
            }

            val lucher = Callback.NewTest(typeOfTest = Lucher)
            val mmpi566 = Callback.NewTest(typeOfTest = Mmpi566)
            val mmpi377 = Callback.NewTest(typeOfTest = Mmpi377)
            val dailyQuiz = Callback.NewTest(typeOfTest = DailyQuiz)

            userConnection.sendMessageWithButtons(
                chatInfo.chatId,
                text = string("choose_test"),
                buttons = listOf(
                    Button(string("lucher"), lucher),
                    Button(string("mmpi_566"), mmpi566),
                    Button(string("mmpi_377"), mmpi377),
                    Button(string("daily_quiz_on"), dailyQuiz)
                )
            )
        }
    }

    fun launchMmpi566(
        chatInfo: ChatInfo
    ) = scope.launch {
        try {
            println("$TAG: launchMmpi566Test();")

            userConnection.notifyAdmin(
                "launchMmpi566Test(); chatInfo = $chatInfo"
            )
            val userId = chatInfo.userId
            val user = CentralDataStorage.usersStorage.getUser(userId)!!

            removeSession(userId)
            sessions[userId] = MmpiSession(
                user = user,
                roomId = roomId,
                chatId = chatInfo.chatId,
                type = Mmpi566,
                userConnection = userConnection,
                onEndedCallback = { removeSession(it.sessionId) }
            )

            user.apply {
                sessions[userId]!!.start()
            }
        } catch (e: Exception) {
            userConnection.notifyAdmin("launchMmpi566Test()", exception = e)
        }
    }

    fun launchMmpi377(
        chatInfo: ChatInfo
    ) = scope.launch {
        try {
            println("$TAG: launchMmpi377Test();")
            val userId = chatInfo.userId
            val user = CentralDataStorage.usersStorage.getUser(userId)!!

            userConnection.notifyAdmin("launchMmpi377Test(); chatInfo = $chatInfo")

            removeSession(userId)
            sessions[userId] = MmpiSession(
                user = user,
                roomId = roomId,
                chatId = chatInfo.chatId,
                type = Mmpi377,
                userConnection = userConnection,
            ) { removeSession(it.sessionId) }

            sessions[userId]!!.start()
        } catch (e: Exception) {
            userConnection.notifyAdmin("launchMmpi377Test()", exception = e)
        }
    }

    fun launchLucher(
        chatInfo: ChatInfo
    ) = scope.launch {
        try {
            println("$TAG: launchLucherTest();")
            val userId = chatInfo.userId
            val chatId = chatInfo.chatId
            val user = CentralDataStorage.usersStorage.getUser(userId)!!

            userConnection.notifyAdmin("launchLucherTest(); chatInfo = $chatInfo")

            removeSession(userId)
            sessions[userId] = LucherSession(
                user = user,
                chatId = chatId,
                roomId = roomId,
                userConnection = userConnection,
            ) { removeSession(it.sessionId) }

            sessions[userId]!!.start()

        } catch (e: Exception) {
            userConnection.notifyAdmin("launchLucherTest()", exception = e)
        }
    }

    fun launchDailyQuiz(
        chatInfo: ChatInfo
    ) = scope.launch {
        try {
            println("$TAG: launchLucherTest();")
            val userId = chatInfo.userId
            val chatId = chatInfo.chatId
            val user = CentralDataStorage.usersStorage.getUser(userId)!!

            userConnection.notifyAdmin("launchLucherTest(); chatInfo = $chatInfo")

            removeSession(userId)
            sessions[userId] = DailyQuizSession(
                user = user,
                chatId = chatId,
                roomId = roomId,
                userConnection = userConnection,
            ) { removeSession(it.sessionId) }

            sessions[userId]!!.start()

        } catch (e: Exception) {
            userConnection.notifyAdmin("launchLucherTest()", exception = e)
        }
    }

    fun callbackQuery(
        chatInfo: ChatInfo,
        data: String
    ) = scope.launch {
        val userId: UserId = chatInfo.userId
        val charId: ChatId = chatInfo.chatId
        val session = sessions[userId]
        val messageId = chatInfo.messageId

        try {
            when (val callback = Callback.fromString(data)) {
                is Callback.GenderAnswer, is Callback.Lucher, is Callback.Mmpi, is Callback.DailyQuiz -> {
                    session?.sendAnswer(callback, messageId)
                }
                is Callback.NewTest -> {
                    userConnection.notifyAdmin("no session with id $userId, just ${formatSessionsList()}")
                    launchTest(
                        chatInfo = chatInfo,
                        type = callback.typeOfTest
                    )
                }
            }
        } catch (e: Exception) {
            userConnection.notifyAdmin("callbackQuery()", exception = e)
            removeSession(chatInfo.userId)
            userConnection.cleanUp(charId, session?.state?.messageIds)
        }
    }

    private fun formatSessionsList(): String {
        val sessionStr = sessions.entries.joinToString(
            ",", "[", "]"
        ) { "${it.key}" }
        return sessionStr
    }

    private suspend fun launchTest(
        chatInfo: ChatInfo,
        type: TypeOfTest
    ) {
        val userId = chatInfo.userId
        val chatId = chatInfo.chatId
        val messageId = chatInfo.messageId
        val user = CentralDataStorage.usersStorage.getUser(userId)!!

        userConnection.notifyAdmin("launchTest($type)")
        userConnection.removeMessage(chatId, messageId)

        sessions[userId] = when (type) {
            Mmpi566, Mmpi377 -> MmpiSession(
                user = user,
                roomId = roomId,
                chatId = chatId,
                type = type,
                userConnection = userConnection,
            ) { removeSession(it.sessionId) }

            Lucher -> LucherSession(
                user = user,
                roomId = roomId,
                chatId = chatId,
                userConnection = userConnection,
            ) { removeSession(it.sessionId) }

            DailyQuiz -> DailyQuizSession(
                user = user,
                roomId = roomId,
                chatId = chatId,
                userConnection = userConnection,
            ) { removeSession(it.sessionId) }
        }
        sessions[userId]!!.start()
    }

    private fun removeSession(userId: SessionId) {
        userConnection.notifyAdmin("removeSession($userId)")
        CentralDataStorage.usersStorage.removeSession(userId)
        sessions.remove(userId)
    }
}