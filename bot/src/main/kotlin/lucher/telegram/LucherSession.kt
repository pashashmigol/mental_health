package lucher.telegram

import Settings.ADMIN_ID
import Settings.LUCHER_TEST_TIMEOUT
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import lucher.*
import models.User
import storage.CentralDataStorage
import telegram.OnEnded
import telegram.TelegramSession
import telegram.helpers.showResult
import telegram.sendError

typealias OnUserChoseColor = (env: CallbackQueryHandlerEnvironment, String) -> Unit

data class LucherSession(
    override val id: Long,
    val onEndedCallback: OnEnded
) : TelegramSession {
    companion object {
        val scope = GlobalScope
    }

    private var onColorChosen: OnUserChoseColor? = null

    override fun start(user: User, chatId: Long, adminBot: Bot, clientBot: Bot) {
        val userId = user.id

        val handler = CoroutineExceptionHandler { _, exception ->
            sendError(userId, "LucherSession error", exception)
        }
        scope.launch(handler) { executeTesting(user, chatId, adminBot, clientBot) }
    }

    override fun onCallbackFromUser(env: CallbackQueryHandlerEnvironment) {
        val answer = env.callbackQuery.data
        onColorChosen?.invoke(env, answer)
    }

    private suspend fun executeTesting(user: User, chatId: Long, adminBot: Bot, clientBot: Bot) {
        val firstRoundAnswers = runRound(chatId, clientBot)
        askUserToWaitBeforeSecondRound(chatId, clientBot, minutes = LUCHER_TEST_TIMEOUT)
        val secondRoundAnswers = runRound(chatId, clientBot)

        val answers = LucherAnswers(firstRoundAnswers, secondRoundAnswers)
        val result = calculateResult(answers, CentralDataStorage.lucherData.meanings)

        val folderLink = CentralDataStorage.reports.saveLucher(
            userId = user.name,
            answers = answers,
            result = result
        )
        onEndedCallback(this)
        showResult(user, ADMIN_ID, folderLink, adminBot, clientBot)
    }

    private suspend fun runRound(chatId: Long, bot: Bot): List<LucherColor> {

        val shownColors = showAllColors(chatId, bot)
        val shownOptions = createReplyOptions()
        askUserToChooseColor(chatId, bot, shownOptions)

        val answers = mutableListOf<String>()
        val channel = Channel<Unit>(0)//using channel to wait until all colors are chosen

        onColorChosen = { callbackEnv, answer: String ->
            removePressedButton(answer, callbackEnv, shownOptions)
            removeChosenColor(callbackEnv, answer, shownColors)

            answers.add(answer)

            if (allColorsChosen(answers)) {
                answers.add(shownColors.first()!!.caption!!)
                cleanUp(callbackEnv, shownColors, shownOptions)

                channel.offer(Unit)
            }
        }
        channel.receive()
        assert(answers.size == LucherColor.values().size) { "wrong answers number" }

        return answers.map { LucherColor.of(it) }
    }
}