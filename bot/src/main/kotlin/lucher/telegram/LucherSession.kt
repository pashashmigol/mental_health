package lucher.telegram

import Settings.LUCHER_TEST_TIMEOUT
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import lucher.*
import models.User
import storage.CentralDataStorage
import storage.CentralDataStorage.string
import telegram.*
import telegram.helpers.showResult
import Result
import com.soywiz.klock.DateTimeTz
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import models.TypeOfTest
import storage.Folder

private typealias OnUserChoseColor = (callback: Callback, messageId: MessageId?) -> Unit

class LucherSession(
    override val user: User,
    override val chatId: Long,
    override val roomId: Long,
    override val userConnection: UserConnection,
    val minutesBetweenRounds: Int = LUCHER_TEST_TIMEOUT,
    override val onEndedCallback: OnEnded
) : TelegramSession<Unit>(
    user = user,
    roomId = chatId,
    chatId = roomId,
    type = TypeOfTest.Lucher,
    userConnection = userConnection,
    onEndedCallback = onEndedCallback
) {

    companion object {
        val scope = GlobalScope
    }

    private var onColorChosen: OnUserChoseColor? = null

    override suspend fun start() {
        val handler = CoroutineExceptionHandler { _, exception ->
            userConnection.notifyAdmin("LucherSession error: ${exception.stackTraceToString()}", exception)
            userConnection.sendMessage(chatId, "LucherSession error: ${exception.stackTraceToString()}")
            userConnection.sendMessage(chatId, string("start_again"))
        }
        scope.launch(handler) { executeTesting(user, chatId) }
    }

    private suspend fun executeTesting(user: User, chatId: Long) {

        val firstRoundAnswers = runRound(chatId, userConnection)
        askUserToWaitBeforeSecondRound(
            chatId,
            minutes = minutesBetweenRounds,
            sessionState = state,
            userConnection = userConnection
        )
        val secondRoundAnswers = runRound(chatId, userConnection)

        val answers = LucherAnswers(
            user = user,
            date = DateTimeTz.nowLocal(),
            firstRound = firstRoundAnswers,
            secondRound = secondRoundAnswers
        )
        val result = calculateLucher(answers, CentralDataStorage.lucherData.meanings)

        val folderLink = CentralDataStorage.saveLucher(
            user = user,
            answers = answers,
            result = result,
            saveAnswers = true
        ) as Result.Success<Folder>

        onEndedCallback(this)

        showResult(user, folderLink.data.link, userConnection)
    }

    private suspend fun runRound(
        chatId: ChatId,
        userConnection: UserConnection?
    ): List<LucherColor> {
        val shownOptions: MutableList<LucherColor> = LucherColor.values().toMutableList()

        val colorIds: List<MessageId>? = userConnection?.sendMessagesWithLucherColors(chatId, LucherColor.values())
            .let {
                state.addMessageIds(it)
                it
            }

        userConnection?.sendMessageWithButtons(
            chatId = chatId,
            text = string("choose_color"),
            buttons = createReplyOptions(shownOptions),
        ).let { state.addMessageId(it) }

        val answers = mutableListOf<LucherColor>()
        val channel = Channel<Unit>(0)//using channel to wait until all colors are chosen

        onColorChosen = { callback: Callback, messageId: MessageId? ->
            callback as Callback.Lucher
            val answer = callback.answer.name
            val index = callback.answer.index

            shownOptions.removeIf { it.name == answer }

            userConnection?.setButtonsForMessage(
                chatId = chatId,
                messageId = messageId,
                buttons = createReplyOptions(shownOptions)
            ).let { state.addMessageId(it) }

            colorIds?.let {messageIds->
                messageIds.elementAtOrNull(index)?.let { messageId ->
                    userConnection?.removeMessage(chatId, messageId)
                }
            }

            answers.add(LucherColor.valueOf(answer))

            if (allColorsChosen(answers)) {
                onColorChosen = null
                userConnection?.cleanUp(
                    chatId = chatId,
                    messageIds = state.messageIds
                )
                channel.offer(Unit)
            }
        }
        channel.receive()
        assert(answers.size == LucherColor.values().size) { "wrong answers number" }

        return answers
    }

    private val mutex = Mutex()
    override suspend fun onAnswer(callback: Callback, messageId: MessageId?): Result<Unit> {
        mutex.withLock {
            var limit = 1000
            while (onColorChosen == null) {
                limit--
                if (limit == 0) {
                    return Result.Error("timeout")
                }
                delay(1)
            }
            onColorChosen?.invoke(callback, messageId) ?: Result.Error("onAnswer is null")
        }
        return Result.Success(Unit)
    }
}