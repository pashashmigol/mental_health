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


private const val TAG = "telegram.WorkSpace"

class TelegramRoom(
    val roomId: Long,
    private val userConnection: UserConnection,
) {
    private val sessions = mutableMapOf<Long, TelegramSession<*>>()
    private val scope = GlobalScope

    suspend fun restoreState() {
        val storedSessionStates = CentralDataStorage.usersStorage.takeAllSessions()

        val storedSessions = (storedSessionStates as Result.Success).data
            .filter {
                roomId == it.roomId
            }
            .map { sessionState ->
                val session = when (sessionState.type) {
                    Mmpi566, Mmpi377 -> MmpiSession(
                        sessionId = sessionState.sessionId,
                        roomId = roomId,
                        type = sessionState.type,
                        userConnection = userConnection,
                        onEndedCallback = { removeSession(it.sessionId) }
                    )
                    Lucher -> LucherSession(
                        sessionId = sessionState.sessionId,
                        roomId = roomId,
                        userConnection = userConnection,
                        onEndedCallback = { removeSession(it.sessionId) }
                    )
                }
                session.applyState(sessionState)

                Pair(session.sessionId, session)
            }
        sessions.putAll(storedSessions)
    }

    fun saveState() {
        val sessionsStates = sessions.map { it.value.state }
        CentralDataStorage.usersStorage.saveAllSessions(sessionsStates)
    }

    fun welcomeUser(
        chatInfo: ChatInfo,
        userConnection: UserConnection
    ): Job {
        return scope.launch {
            val userId = chatInfo.userId

            userConnection.notifyAdmin(
                "welcomeUser(); chatInfo = $chatInfo"
            )
            if (CentralDataStorage.usersStorage.hasUserWithId(userId)) {
                val user = CentralDataStorage.usersStorage.getUser(userId)

                userConnection.notifyAdmin(
                    "user already exists: $user"
                )
            } else {
                CentralDataStorage.createUser(userId, chatInfo.userName)
                val user = CentralDataStorage.usersStorage.getUser(userId)

                userConnection.notifyAdmin(
                    "user created: $user"
                )
            }

            userConnection.sendMessageWithButtons(
                chatInfo.chatId,
                text = string("choose_test"),
                buttons = listOf(
                    Button(string("lucher"), Lucher.name),
                    Button(string("mmpi_566"), Mmpi566.name),
                    Button(string("mmpi_377"), Mmpi377.name)
                )
            )
        }
    }

    fun launchMmpi566Test(
        chatInfo: ChatInfo
    ) = scope.launch {
        try {
            println("$TAG: launchMmpi566Test();")

            userConnection.notifyAdmin(
                "launchMmpi566Test(); chatInfo = $chatInfo"
            )

            val userId = chatInfo.userId

            removeSession(userId)
            sessions[userId] = MmpiSession(
                sessionId = userId,
                roomId = roomId,
                type = Mmpi566,
                userConnection = userConnection,
                onEndedCallback = { removeSession(it.sessionId) }
            )

            val user = CentralDataStorage.usersStorage.getUser(userId)

            user?.apply {
                sessions[userId]!!.start(
                    user = user,
                    chatId = chatInfo.chatId
                )
            }
        } catch (e: Exception) {
            userConnection.notifyAdmin("launchMmpi566Test()", exception = e)
        }
    }

    fun launchMmpi377Test(
        chatInfo: ChatInfo
    ) = scope.launch {
        try {
            println("$TAG: launchMmpi377Test();")
            val userId = chatInfo.userId
            val user = CentralDataStorage.usersStorage.getUser(userId)!!

            userConnection.notifyAdmin("launchMmpi377Test(); chatInfo = $chatInfo")

            removeSession(userId)
            sessions[userId] = MmpiSession(
                sessionId = userId,
                roomId = roomId,
                type = Mmpi377,
                userConnection = userConnection,
            ) { removeSession(it.sessionId) }

            sessions[userId]!!.start(
                user = user,
                chatId = chatInfo.chatId
            )
        } catch (e: Exception) {
            userConnection.notifyAdmin("launchMmpi377Test()", exception = e)
        }
    }

    fun launchLucherTest(
        chatInfo: ChatInfo
    ) = scope.launch {
        try {
            println("$TAG: launchLucherTest();")
            val userId = chatInfo.userId
            val user = CentralDataStorage.usersStorage.getUser(userId)!!

            userConnection.notifyAdmin("launchLucherTest(); chatInfo = $chatInfo")

            removeSession(userId)
            sessions[userId] = LucherSession(
                userId,
                roomId = roomId,
                userConnection,
            ) { removeSession(it.sessionId) }

            sessions[userId]!!.start(
                user = user,
                chatId = chatInfo.chatId
            )

        } catch (e: Exception) {
            userConnection.notifyAdmin("launchLucherTest()", exception = e)
        }
    }

    fun callbackQuery(
        chatInfo: ChatInfo,
        data: String
    ) = scope.launch {
        try {
            val userId = chatInfo.userId
            val session = sessions[userId]

            if (session != null) {
                session.onCallbackFromUser(
                    messageId = chatInfo.messageId,
                    data = data
                )
            } else {
                val sessionStr = sessions.entries.joinToString(
                    ",", "[", "]"
                ) { "${it.key}" }
                userConnection.notifyAdmin("no session with id $userId, just $sessionStr")
                launchTest(
                    chatInfo = chatInfo,
                    data = data
                )
            }
        } catch (e: Exception) {
            val userId = chatInfo.userId
            userConnection.notifyAdmin("callbackQuery()", exception = e)
            removeSession(userId)
            userConnection.cleanUp()
        }
    }

    private suspend fun launchTest(
        chatInfo: ChatInfo,
        data: String
    ) {
        val type = valueOf(data)
        val userId = chatInfo.userId
        val chatId = chatInfo.chatId
        val messageId = chatInfo.messageId
        val user = CentralDataStorage.usersStorage.getUser(userId)!!

        userConnection.removeMessage(chatId, messageId)

        sessions[userId] = when (type) {
            Mmpi566, Mmpi377 -> MmpiSession(
                sessionId = userId,
                roomId = roomId,
                type = type,
                userConnection = userConnection,
            ) { removeSession(it.sessionId) }

            Lucher -> LucherSession(
                sessionId = userId,
                roomId = roomId,
                userConnection = userConnection,
            ) { removeSession(it.sessionId) }
        }
        sessions[userId]!!.start(
            user = user,
            chatId = chatId
        )
    }

    private fun removeSession(userId: Long) {
        userConnection.notifyAdmin("sessions.remove($userId)")
        sessions.remove(userId)
    }
}