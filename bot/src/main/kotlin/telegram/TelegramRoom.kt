package telegram

import lucher.telegram.LucherSession
import mmpi.telegram.MmpiSession
import models.TypeOfTest.*

import io.ktor.util.*
import io.ktor.util.collections.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import lucher.LucherData
import mmpi.MmpiData
import models.TypeOfTest
import models.User
import quiz.DailyQuizData
import quiz.DailyQuizSession
import storage.Fonts
import storage.R
import storage.ReportStorage
import storage.users.UserStorage
import storage.users.createUser


private const val TAG = "telegram.WorkSpace"

@InternalAPI
class TelegramRoom(
    val roomId: Long,
    private val userConnection: UserConnection,
    private val reportStorage: ReportStorage,
    private val userStorage: UserStorage,
    private val lusherData: LucherData,
    private val mmpiData566: MmpiData,
    private val mmpiData377: MmpiData,
    private val dailyQuizData: DailyQuizData,
) {
    internal val sessions = ConcurrentMap<Long, TelegramSession<*>>()
    private val scope = GlobalScope

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        userConnection.notifyAdmin("TelegramRoom error: ${exception.stackTraceToString()}", exception)
        print("TelegramRoom error: ${exception.message}")
        exception.printStack()
    }

    fun restoreState() = runBlocking(exceptionHandler) {
        val storedSessionStates = userStorage
            .takeAllSessions()
            .dealWithError {
                println("TelegramRoom error: ${it.message}")
                it.exception?.printStackTrace()
                return@runBlocking
            }
        val storedSessions = storedSessionStates
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
        val user = userStorage.getUser(userId)!!

        val session = when (sessionState.type) {
            Mmpi566 -> MmpiSession(
                user = user,
                roomId = roomId,
                chatId = sessionState.chatId,
                type = sessionState.type,
                userConnection = userConnection,
                userStorage = userStorage,
                reportStorage = reportStorage,
                mmpiData = mmpiData566,
                onEndedCallback = { removeSession(it.sessionId) }
            )
            Mmpi377 -> MmpiSession(
                user = user,
                roomId = roomId,
                chatId = sessionState.chatId,
                type = sessionState.type,
                userConnection = userConnection,
                userStorage = userStorage,
                reportStorage = reportStorage,
                mmpiData = mmpiData377,
                onEndedCallback = { removeSession(it.sessionId) }
            )
            Lucher -> LucherSession(
                user = user,
                roomId = roomId,
                chatId = sessionState.chatId,
                userConnection = userConnection,
                userStorage = userStorage,
                reportStorage = reportStorage,
                onEndedCallback = { removeSession(it.sessionId) },
                lucherData = lusherData
            )
            DailyQuiz -> DailyQuizSession(
                user = user,
                roomId = roomId,
                chatId = sessionState.chatId,
                dayTime = DailyQuizSession.Time.MORNING,
                userConnection = userConnection,
                userStorage = userStorage,
                reportStorage = reportStorage,
                dailyQuizData = dailyQuizData,
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
            val userName = chatInfo.userName
            val user: User

            if (userStorage.hasUserWithId(userId)) {
                user = userStorage.getUser(userId)!!
                userConnection.notifyAdmin(
                    "user already exists: $user"
                )
            } else {
                createUser(
                    userId = userId,
                    userName = userName,
                    reportStorage = reportStorage,
                    userStorage = userStorage
                )
                user = userStorage.getUser(userId)!!

                userConnection.notifyAdmin(
                    "user created: $user"
                )
            }

            val lucher = UserAnswer.NewTest(typeOfTest = Lucher)
            val mmpi566 = UserAnswer.NewTest(typeOfTest = Mmpi566)
            val mmpi377 = UserAnswer.NewTest(typeOfTest = Mmpi377)

            val (dailyQuizSwitchText, dailyQuizSwitch) = if (user.runDailyQuiz) {
                Pair(R.string("daily_quiz_off"), UserAnswer.Switch(on = false))
            } else {
                Pair(R.string("daily_quiz_on"), UserAnswer.Switch(on = true))
            }

            val dailyQuizButton = Button(dailyQuizSwitchText, dailyQuizSwitch)

            userConnection.sendMessageWithButtons(
                chatId = chatInfo.chatId,
                text = R.string("choose_test"),
                buttons = listOf(
                    Button(R.string("lucher"), lucher),
                    Button(R.string("mmpi_566"), mmpi566),
                    Button(R.string("mmpi_377"), mmpi377),
                    dailyQuizButton
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
            val user = userStorage.getUser(userId)!!

            removeSession(userId)
            sessions[userId] = MmpiSession(
                user = user,
                roomId = roomId,
                chatId = chatInfo.chatId,
                type = Mmpi566,
                userConnection = userConnection,
                userStorage = userStorage,
                reportStorage = reportStorage,
                mmpiData = mmpiData566,
                onEndedCallback = { removeSession(it.sessionId) }
            )
            sessions[userId]!!.start()

        } catch (e: Exception) {
            userConnection.notifyAdmin("launchMmpi566Test()", exception = e)
        }
    }

    fun launchMmpi377(
        chatInfo: ChatInfo
    ) = scope.launch(exceptionHandler) {
        println("$TAG: launchMmpi377Test();")
        val userId = chatInfo.userId
        val user = userStorage.getUser(userId)!!

        userConnection.notifyAdmin("launchMmpi377Test(); chatInfo = $chatInfo")

        removeSession(userId)
        sessions[userId] = MmpiSession(
            user = user,
            roomId = roomId,
            chatId = chatInfo.chatId,
            type = Mmpi377,
            userConnection = userConnection,
            userStorage = userStorage,
            reportStorage = reportStorage,
            mmpiData = mmpiData377,
        ) { removeSession(it.sessionId) }

        sessions[userId]!!.start()
    }

    fun launchLucher(
        chatInfo: ChatInfo
    ) = scope.launch(exceptionHandler) {
        println("$TAG: launchLucherTest();")
        val userId = chatInfo.userId
        val chatId = chatInfo.chatId
        val user = userStorage.getUser(userId)!!

        userConnection.notifyAdmin("launchLucherTest(); chatInfo = $chatInfo")

        removeSession(userId)
        sessions[userId] = LucherSession(
            user = user,
            chatId = chatId,
            roomId = roomId,
            userConnection = userConnection,
            userStorage = userStorage,
            reportStorage = reportStorage,
            lucherData = lusherData,
        ) { removeSession(it.sessionId) }

        sessions[userId]!!.start()
    }

    fun onMorningChron() {
        userStorage.allUsers().forEach { user ->
            val chatInfo = ChatInfo(
                userId = user.id,
                userName = user.name,
                chatId = user.id,
                messageId = NOT_SENT,
            )
            launchDailyQuiz(chatInfo = chatInfo, dayTime = DailyQuizSession.Time.MORNING)
        }
    }

    fun onEveningChron() {
        userStorage.allUsers()
            .filter {
                it.runDailyQuiz
            }.forEach { user ->
                val chatInfo = ChatInfo(
                    userId = user.id,
                    userName = user.name,
                    chatId = user.id,
                    messageId = NOT_SENT,
                )
                launchDailyQuiz(
                    chatInfo = chatInfo,
                    dayTime = DailyQuizSession.Time.EVENING
                )
            }
    }

    fun launchDailyQuiz(
        chatInfo: ChatInfo,
        dayTime: DailyQuizSession.Time
    ) = scope.launch(exceptionHandler) {
        println("$TAG: launchLucherTest();")
        val userId = chatInfo.userId
        val chatId = chatInfo.chatId
        val user = userStorage.getUser(userId)!!

        userConnection.notifyAdmin("launchLucherTest(); chatInfo = $chatInfo")

        removeSession(userId)
        sessions[userId] = DailyQuizSession(
            user = user,
            chatId = chatId,
            dayTime = dayTime,
            roomId = roomId,
            userConnection = userConnection,
            userStorage = userStorage,
            reportStorage = reportStorage,
            dailyQuizData = dailyQuizData
        ) { removeSession(it.sessionId) }

        sessions[userId]!!.start()
    }

    fun onCallbackQuery(
        chatInfo: ChatInfo,
        data: String
    ) = scope.launch(exceptionHandler) {
        val userId: UserId = chatInfo.userId
        val charId: ChatId = chatInfo.chatId
        val session = sessions[userId]
        val messageId = chatInfo.messageId

        try {
            when (val userAnswer: UserAnswer = UserAnswer.fromString(data)) {
                is UserAnswer.GenderAnswer,
                is UserAnswer.Lucher,
                is UserAnswer.Mmpi,
                is UserAnswer.DailyQuiz -> {
                    session?.sendAnswer(userAnswer, messageId)
                }
                is UserAnswer.NewTest -> {
                    userConnection.notifyAdmin("no session with id $userId, just ${formatSessionsList()}")
                    launchTest(
                        chatInfo = chatInfo,
                        type = userAnswer.typeOfTest
                    )
                }
                is UserAnswer.Switch -> onSwitch(chatInfo, userConnection, userAnswer)
            }
        } catch (e: Exception) {
            userConnection.notifyAdmin("callbackQuery()", exception = e)
            removeSession(chatInfo.userId)
            userConnection.cleanUp(charId, session?.state?.messageIds)
        }
    }

    private fun onSwitch(
        chatInfo: ChatInfo,
        userConnection: UserConnection,
        switch: UserAnswer.Switch
    ) {
        val lucher = UserAnswer.NewTest(typeOfTest = Lucher)
        val mmpi566 = UserAnswer.NewTest(typeOfTest = Mmpi566)
        val mmpi377 = UserAnswer.NewTest(typeOfTest = Mmpi377)

        val user = userStorage.getUser(chatInfo.userId)!!

        val (dailyQuizSwitchText, dailyQuizSwitch) = if (switch.on) {
            Pair(R.string("daily_quiz_off"), UserAnswer.Switch(on = false))
        } else {
            Pair(R.string("daily_quiz_on"), UserAnswer.Switch(on = true))
        }

        val dailyQuizButton = Button(dailyQuizSwitchText, dailyQuizSwitch)

        val updatedUser = user.copy(runDailyQuiz = switch.on)

        scope.launch(exceptionHandler) {
            userStorage.saveUser(updatedUser)
        }

        userConnection.updateMessage(
            chatId = chatInfo.chatId,
            messageId = chatInfo.messageId,
            text = R.string("choose_test"),
            buttons = listOf(
                Button(R.string("lucher"), lucher),
                Button(R.string("mmpi_566"), mmpi566),
                Button(R.string("mmpi_377"), mmpi377),
                dailyQuizButton
            )
        )
    }

    fun onMessage(
        chatInfo: ChatInfo,
        message: String?
    ) = scope.launch(exceptionHandler) {
        val userId: UserId = chatInfo.userId
        val session = sessions[userId]
        val messageId = chatInfo.messageId
        val answer = UserAnswer.Text(message ?: "")

        session?.sendAnswer(answer, messageId)
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
        val user = userStorage.getUser(userId)!!

        userConnection.notifyAdmin("launchTest($type)")
        userConnection.removeMessage(chatId, messageId)

        sessions[userId] = when (type) {
            Mmpi566 -> MmpiSession(
                user = user,
                roomId = roomId,
                chatId = chatId,
                type = type,
                userConnection = userConnection,
                userStorage = userStorage,
                reportStorage = reportStorage,
                mmpiData = mmpiData566,
            ) { removeSession(it.sessionId) }

            Mmpi377 -> MmpiSession(
                user = user,
                roomId = roomId,
                chatId = chatId,
                type = type,
                userConnection = userConnection,
                userStorage = userStorage,
                reportStorage = reportStorage,
                mmpiData = mmpiData377,
            ) { removeSession(it.sessionId) }

            Lucher -> LucherSession(
                user = user,
                roomId = roomId,
                chatId = chatId,
                userConnection = userConnection,
                userStorage = userStorage,
                reportStorage = reportStorage,
                lucherData = lusherData,
            ) { removeSession(it.sessionId) }

            DailyQuiz -> DailyQuizSession(
                user = user,
                roomId = roomId,
                chatId = chatId,
                dayTime = DailyQuizSession.Time.MORNING,
                userConnection = userConnection,
                userStorage = userStorage,
                reportStorage = reportStorage,
                dailyQuizData = dailyQuizData
            ) { removeSession(it.sessionId) }
        }
        sessions[userId]!!.start()
    }

    private fun removeSession(userId: SessionId) {
        userConnection.notifyAdmin("removeSession($userId)")
        userStorage.removeSession(userId)
        sessions.remove(userId)
    }
}