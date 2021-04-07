package lucher.telegram

import Settings.ADMIN_ID
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

typealias OnUserChoseColor = (connection: UserConnection, messageId: Long, data: String) -> Unit

data class LucherSession(
    override val id: Long,
    val clientConnection: UserConnection,
    val adminConnection: UserConnection,
    val onEndedCallback: OnEnded
) : TelegramSession<Unit> {
    companion object {
        val scope = GlobalScope
    }

    private var onColorChosen: OnUserChoseColor? = null

    override suspend fun start(user: User, chatId: Long) {
        val handler = CoroutineExceptionHandler { _, exception ->
            notifyAdmin("LucherSession error", exception)
        }
        scope.launch(handler) { executeTesting(user, chatId) }
    }

    override suspend fun onCallbackFromUser(messageId: Long, data: String): Result<Unit> {
        val chosen = onColorChosen
        return if (chosen == null) {
            Result.Error("onAnswer is null")
        } else {
            chosen.invoke(clientConnection, messageId, data)
            Result.Success(data = Unit)
        }
    }

    private suspend fun executeTesting(user: User, chatId: Long) {
        val firstRoundAnswers = runRound(chatId, this.clientConnection)

        askUserToWaitBeforeSecondRound(chatId, minutes = LUCHER_TEST_TIMEOUT, clientConnection)
        val secondRoundAnswers = runRound(chatId, this.clientConnection)

        val answers = LucherAnswers(firstRoundAnswers, secondRoundAnswers)
        val result = calculateResult(answers, CentralDataStorage.lucherData.meanings)

        val folderLink = CentralDataStorage.reports.saveLucher(
            userId = user.name,
            answers = answers,
            result = result
        )
        onEndedCallback(this)
        showResult(user, ADMIN_ID, folderLink, clientConnection, adminConnection)
    }

    private suspend fun runRound(chatId: Long, userConnection: UserConnection): List<LucherColor> {

        showAllColors(chatId, userConnection)
        val shownOptions: MutableList<Button> = createReplyOptions()

        userConnection.sendMessageWithButtons(
            chatId = chatId,
            text = string("choose_color"),
            buttons = shownOptions,
            placeButtonsVertically = true
        )

        val answers = mutableListOf<LucherColor>()
        val channel = Channel<Unit>(0)//using channel to wait until all colors are chosen


        onColorChosen = { connection: UserConnection, messageId: Long, answer: String ->

            shownOptions.removeIf { it.data == answer }
            connection.setButtonsForMessage(
                chatId = chatId, messageId = messageId, buttons = shownOptions
            )

            answers.add(LucherColor.of(answer))

            if (allColorsChosen(answers)) {
                val lastShownOption: String = shownOptions.first().data
                answers.add(LucherColor.of(lastShownOption))
                connection.cleanUp()
                channel.offer(Unit)
            }
        }
        channel.receive()
        assert(answers.size == LucherColor.values().size) { "wrong answers number" }

        return answers
    }
}