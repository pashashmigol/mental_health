package telegram

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lucher.telegram.LucherSession
import mmpi.telegram.MmpiSession
import mmpi.telegram.MmpiTestingSession
import models.Type
import storage.CentralDataStorage
import storage.CentralDataStorage.string


private const val TAG = "telegram.WorkSpace"

class TelegramRoom(
    private val clientConnection: UserConnection,
    private val adminConnection: UserConnection
) {
    private val sessions = mutableMapOf<Long, TelegramSession>()
    private val scope = GlobalScope

    fun welcomeNewUser(
        chatInfo: ChatInfo,
        userConnection: UserConnection
    ) = scope.launch {
        CentralDataStorage.createUser(chatInfo.userId, chatInfo.userName)

        notifyAdmin(
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
        chatInfo: ChatInfo
    ) = scope.launch {
        try {
            println("$TAG: launchMmpi566Test();")

            notifyAdmin(
                message = "launchMmpi566Test(); chatInfo = $chatInfo"
            )

            val userId = chatInfo.userId

            removeSession(userId)
            sessions[userId] = MmpiSession(
                userId,
                Type.Mmpi566,
                clientConnection,
                adminConnection,
                onEndedCallback = { removeSession(it.id) }
            )

            val user = CentralDataStorage.users.get(userId)

            user?.apply {
                sessions[userId]!!.start(
                    user = user,
                    chatId = chatInfo.chatId
                )
            }
        } catch (e: Exception) {
            notifyAdmin(exception = e)
        }
    }

    fun launchMmpi377Test(
        chatInfo: ChatInfo
    ) = scope.launch {
        try {
            println("$TAG: launchMmpi377Test();")
            val userId = chatInfo.userId
            val user = CentralDataStorage.users.get(userId)!!

            notifyAdmin(message = "launchMmpi377Test(); chatInfo = $chatInfo")

            removeSession(userId)
            sessions[userId] = MmpiSession(
                userId,
                Type.Mmpi377,
                clientConnection,
                adminConnection
            ) { removeSession(it.id) }

            sessions[userId]!!.start(
                user = user,
                chatId = chatInfo.chatId
            )
        } catch (e: Exception) {
            notifyAdmin(exception = e)
        }
    }

    fun launchMmpiMockTest(
        chatInfo: ChatInfo
    ) = scope.launch {
        try {
            println("$TAG: launchMmpiTest();")
            val userId = chatInfo.userId
            val user = CentralDataStorage.users.get(userId)!!

            notifyAdmin(message = "launchMmpiMockTest(); chatInfo = $chatInfo")

            removeSession(userId)
            sessions[userId] = MmpiTestingSession(
                userId,
                clientConnection,
                adminConnection
            ) { removeSession(it.id) }

            sessions[userId]!!.start(
                user = user,
                chatId = chatInfo.chatId
            )
        } catch (e: Exception) {
            notifyAdmin(exception = e)
        }
    }

    fun launchLucherTest(
        chatInfo: ChatInfo
    ) = scope.launch {
        try {
            println("$TAG: launchLucherTest();")
            val userId = chatInfo.userId
            val user = CentralDataStorage.users.get(userId)!!

            notifyAdmin(message = "launchLucherTest(); chatInfo = $chatInfo")

            removeSession(userId)
            sessions[userId] = LucherSession(
                userId,
                clientConnection,
                adminConnection
            ) { removeSession(it.id) }

            sessions[userId]!!.start(
                user = user,
                chatId = chatInfo.chatId
            )

        } catch (e: Exception) {
            notifyAdmin(exception = e)
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
                notifyAdmin(message = "no session with id $userId, just $sessionStr")
                launchTest(
                    chatInfo = chatInfo,
                    data = data
                )
            }
        } catch (e: Exception) {
            val userId = chatInfo.userId
            notifyAdmin(exception = e)
            removeSession(userId)
            clientConnection.cleanUp()
        }
    }

    private fun launchTest(
        chatInfo: ChatInfo,
        data: String
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
            ) { removeSession(it.id) }

            Type.Lucher -> LucherSession(
                userId,
                clientConnection,
                adminConnection
            ) { removeSession(it.id) }
        }
        sessions[userId]!!.start(
            user = user,
            chatId = chatId
        )
    }

    private fun removeSession(userId: Long) {
        notifyAdmin(message = "sessions.remove($userId)")
        sessions.remove(userId)
    }
}