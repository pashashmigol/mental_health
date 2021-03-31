package telegram

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lucher.telegram.LucherSession
import mmpi.telegram.MmpiSession
import mmpi.telegram.MmpiTestingSession
import models.Type
import storage.CentralDataStorage
import storage.CentralDataStorage.string


object TelegramRoom {
    private const val TAG = "telegram.WorkSpace"
    private val sessions = mutableMapOf<Long, TelegramSession>()
    private val scope = GlobalScope


    fun welcomeNewUser(
        chatInfo: ChatInfo,
        userConnection: UserConnection
    ) = scope.launch {
        CentralDataStorage.createUser(chatInfo.userId, chatInfo.userName)

        sendError(
            to = chatInfo.userId,
            message = "welcomeNewUser(); chatInfo = $chatInfo"
        )

        userConnection.sendMessageWithButtons(
            chatInfo.chatId,
            text = string("choose_test"),
            buttons = listOf(
                Button(string("lucher"), Type.Lucher.name),
                Button(string("mmpi_566"), Type.Mmpi566.name),
                Button(string("mmpi_377"), Type.Mmpi377.name)
            )
        )
    }

    fun launchMmpi566Test(
        chatInfo: ChatInfo,
        clientConnection: UserConnection,
        adminConnection: UserConnection
    ) = scope.launch {
        try {
            println("$TAG: launchMmpi566Test();")

            sendError(
                to = chatInfo.userId,
                message = "launchMmpi566Test(); chatInfo = $chatInfo"
            )

            val userId = chatInfo.userId

            sessions.remove(userId)
            sessions[userId] = MmpiSession(
                userId,
                Type.Mmpi566,
                clientConnection,
                adminConnection,
                onEndedCallback = { sessions.remove(it.id) }
            )

            val user = CentralDataStorage.users.get(userId)

            user?.apply {
                sessions[userId]!!.start(
                    user = user,
                    chatId = chatInfo.chatId
                )
            }
        } catch (e: Exception) {
            sendError(to = chatInfo.userId, exception = e)
        }
    }

    fun launchMmpi377Test(
        chatInfo: ChatInfo,
        clientConnection: UserConnection,
        adminConnection: UserConnection
    ) = scope.launch {
        try {
            println("$TAG: launchMmpi377Test();")
            val userId = chatInfo.userId
            val user = CentralDataStorage.users.get(userId)!!

            sendError(
                to = userId,
                message = "launchMmpi377Test(); chatInfo = $chatInfo"
            )

            sessions.remove(userId)
            sessions[userId] = MmpiSession(
                userId,
                Type.Mmpi377,
                clientConnection,
                adminConnection
            ) { sessions.remove(it.id) }

            sessions[userId]!!.start(
                user = user,
                chatId = chatInfo.chatId
            )
        } catch (e: Exception) {
            sendError(
                to = chatInfo.userId,
                exception = e
            )
        }
    }

    fun launchMmpiMockTest(
        chatInfo: ChatInfo,
        clientConnection: UserConnection,
        adminConnection: UserConnection
    ) = scope.launch {
        try {
            println("$TAG: launchMmpiTest();")
            val userId = chatInfo.userId
            val user = CentralDataStorage.users.get(userId)!!

            sendError(
                to = userId,
                message = "launchMmpiMockTest(); chatInfo = $chatInfo"
            )

            sessions.remove(userId)
            sessions[userId] = MmpiTestingSession(
                userId,
                clientConnection,
                adminConnection
            ) { sessions.remove(it.id) }

            sessions[userId]!!.start(
                user = user,
                chatId = chatInfo.chatId
            )
        } catch (e: Exception) {
            sendError(to = chatInfo.userId, exception = e)
        }
    }

    fun launchLucherTest(
        chatInfo: ChatInfo,
        clientConnection: UserConnection,
        adminConnection: UserConnection
    ) = scope.launch {
        try {
            println("$TAG: launchLucherTest();")
            val userId = chatInfo.userId
            val user = CentralDataStorage.users.get(userId)!!

            sendError(
                to = userId,
                message = "launchLucherTest(); chatInfo = $chatInfo"
            )

            sessions.remove(userId)
            sessions[userId] = LucherSession(
                userId,
                clientConnection,
                adminConnection
            ) { sessions.remove(it.id) }

            sessions[userId]!!.start(
                user = user,
                chatId = chatInfo.chatId
            )

        } catch (e: Exception) {
            sendError(to = chatInfo.userId, exception = e)
        }
    }

    fun callbackQuery(
        chatInfo: ChatInfo,
        data: String,
        clientConnection: UserConnection,
        adminConnection: UserConnection
    ) = scope.launch {
        try {
            val userId = chatInfo.userId
            val session = sessions[userId]

            val sessionStr = sessions.entries.joinToString(
                ",", "[", "]"
            ) { "${it.key}" }

            sendError(
                to = userId,
                message = "callbackQuery(); userId = $userId, sessions = $sessionStr",
            )

            if (session != null) {
                session.onCallbackFromUser(
                    messageId = chatInfo.messageId,
                    data = data
                )
            } else {
                launchTest(
                    chatInfo = chatInfo,
                    data = data,
                    clientConnection = clientConnection,
                    adminConnection = adminConnection
                )
            }
        } catch (e: Exception) {
            val userId = chatInfo.userId
            sessions.remove(userId)
            sendError(userId, exception = e)
        }
    }

    private fun launchTest(
        chatInfo: ChatInfo,
        data: String,
        clientConnection: UserConnection,
        adminConnection: UserConnection
    ) {
        val type = Type.valueOf(data)
        val userId = chatInfo.userId
        val chatId = chatInfo.chatId
        val messageId = chatInfo.messageId
        val user = CentralDataStorage.users.get(userId)!!

        clientConnection.removeMessage(chatId, messageId)

        sessions[userId] = when (type) {
            Type.Mmpi566, Type.Mmpi377 -> MmpiSession(
                userId,
                type,
                clientConnection,
                adminConnection
            ) { sessions.remove(it.id) }

            Type.Lucher -> LucherSession(
                userId,
                clientConnection,
                adminConnection
            ) { sessions.remove(it.id) }
        }
        sessions[userId]!!.start(
            user = user,
            chatId = chatId
        )
    }
}