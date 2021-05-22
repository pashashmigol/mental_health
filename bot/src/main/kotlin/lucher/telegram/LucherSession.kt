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
import com.soywiz.klock.DateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

typealias OnUserChoseColor = (connection: UserConnection, messageId: Long, data: String) -> Unit

data class LucherSession(
    override val id: Long,
    val userConnection: UserConnection,
    val minutesBetweenRounds: Int = LUCHER_TEST_TIMEOUT,
    val onEndedCallback: OnEnded
) : TelegramSession<Unit> {
    companion object {
        val scope = GlobalScope
    }

    private var onColorChosen: OnUserChoseColor? = null

    override suspend fun start(user: User, chatId: Long) {
        val handler = CoroutineExceptionHandler { _, exception ->
            userConnection.notifyAdmin("LucherSession error: ${exception.message}", exception)
        }
        scope.launch(handler) { executeTesting(user, chatId) }
    }

    private suspend fun executeTesting(user: User, chatId: Long) {

        val firstRoundAnswers = runRound(chatId, this.userConnection)

        askUserToWaitBeforeSecondRound(chatId, minutes = LUCHER_TEST_TIMEOUT, userConnection)
        val secondRoundAnswers = runRound(chatId, this.userConnection)

        val answers = LucherAnswers(
            user = user,
            dateTime = DateTime.now(),
            firstRound = firstRoundAnswers,
            secondRound = secondRoundAnswers
        )
        val result = calculateResult(answers, CentralDataStorage.lucherData.meanings)

        val folderLink = CentralDataStorage.saveLucher(
            user = user,
            answers = answers,
            result = result
        )
        onEndedCallback(this)
        showResult(user, folderLink, userConnection)
    }

    private suspend fun runRound(chatId: Long, userConnection: UserConnection): List<LucherColor> {
        val shownOptions: MutableList<LucherColor> = LucherColor.values().toMutableList()
        userConnection.sendMessageWithLucherColors(chatId, LucherColor.values())

        userConnection.sendMessageWithButtons(
            chatId = chatId,
            text = string("choose_color"),
            buttons = createReplyOptions(shownOptions),
        )

        val answers = mutableListOf<LucherColor>()
        val channel = Channel<Unit>(0)//using channel to wait until all colors are chosen

        onColorChosen = { connection: UserConnection, messageId: Long, answer: String ->
            shownOptions.removeIf { it.name == answer }

            connection.setButtonsForMessage(
                chatId = chatId,
                messageId = messageId,
                buttons = createReplyOptions(shownOptions)
            )

            answers.add(LucherColor.valueOf(answer))

            if (allColorsChosen(answers)) {
                onColorChosen = null
                val lastShownOption = shownOptions.first()
                answers.add(lastShownOption)
                connection.cleanUp()
                channel.offer(Unit)
            }
        }
        channel.receive()
        assert(answers.size == LucherColor.values().size) { "wrong answers number" }

        return answers
    }

    private val mutex = Mutex()
    override suspend fun onCallbackFromUser(messageId: Long, data: String): Result<Unit> {
        mutex.withLock {
            while (onColorChosen == null) {
                delay(1)
            }
            onColorChosen?.invoke(userConnection, messageId, data) ?: Result.Error("onAnswer is null")
        }
        return Result.Success(Unit)
    }
}