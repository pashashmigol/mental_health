package lucher.telegram

import Settings.LUCHER_TEST_TIMEOUT
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lucher.*
import models.User
import storage.CentralDataStorage
import storage.CentralDataStorage.string
import telegram.*
import telegram.helpers.showResult
import Result
import com.soywiz.klock.DateTimeTz
import models.TypeOfTest
import storage.Folder


class LucherSession(
    user: User,
    chatId: Long,
    roomId: Long,
    userConnection: UserConnection,
    val minutesBetweenRounds: Int = LUCHER_TEST_TIMEOUT,
    onEndedCallback: OnEnded
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

        val colorIds: List<MessageId>? = userConnection
            ?.sendMessagesWithLucherColors(chatId, LucherColor.values())
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

        while (!allColorsChosen(answers)) {
            val quizButtonClick = waitForAnswer()
            val quizButton = quizButtonClick.quizButton as QuizButton.Lucher

            val answer = quizButton.answer.name
            shownOptions.removeIf { it.name == answer }

            userConnection?.setButtonsForMessage(
                chatId = chatId,
                messageId = quizButtonClick.messageId,
                buttons = createReplyOptions(shownOptions)
            ).let { state.addMessageId(it) }

            colorIds?.let { messageIds ->
                val index = quizButton.answer.index
                messageIds.elementAtOrNull(index)?.let { messageId ->
                    userConnection?.removeMessage(chatId, messageId)
                }
            }
            answers.add(LucherColor.valueOf(answer))
        }
        userConnection?.cleanUp(
            chatId = chatId,
            messageIds = state.messageIds
        )
        assert(answers.size == LucherColor.values().size) { "wrong answers number" }

        return answers
    }
}