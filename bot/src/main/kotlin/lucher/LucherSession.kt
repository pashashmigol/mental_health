package lucher

import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import telegram.OnEnded
import telegram.TelegramSession
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

    override fun start(env: CommandHandlerEnvironment) {
        val handler = CoroutineExceptionHandler { _, exception ->
            sendError(env.bot, env.message.from!!.id, "TelegramSession error", exception)
        }
        scope.launch(handler) { executeTesting(env) }
    }

    override fun onCallbackFromUser(env: CallbackQueryHandlerEnvironment) {
        val answer = env.callbackQuery.data
        onColorChosen?.invoke(env, answer)
    }

    private suspend fun executeTesting(env: CommandHandlerEnvironment) {

        val firstRoundAnswers = runRound(env)
        askUserToWaitBeforeSecondRound(env, minutes = 1)
        val secondRoundAnswers = runRound(env)

        val answers = LucherAnswers(firstRoundAnswers, secondRoundAnswers)
        val result = calculateResult(answers)

        showResult(env, result)
    }

    private suspend fun runRound(env: CommandHandlerEnvironment): List<LucherColor> {

        val shownColors = showAllColors(env)
        val shownOptions = createReplyOptions()
        askUserToChooseColor(env, shownOptions)

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