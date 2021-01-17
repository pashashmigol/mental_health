package telegram

import Gender
import Message
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.PollAnswerHandlerEnvironment
import mmpi.MmpiTestingProcess

data class MmpiSession(
    override val id: Long,
    val callback: OnEnded
) : TelegramSession {

    private var genderPollId: Long = 0L
    private var ongoingProcess: MmpiTestingProcess? = null

    override fun pollAnswer(env: PollAnswerHandlerEnvironment) {
//        if (env.pollAnswer.pollId == genderPollId) {
//            val gender = Gender.valueOf(env.pollAnswer.optionIds[0].toString())
//            ongoingProcess = MmpiTestingProcess(gender)
//        } else {
//            ongoingProcess!!.submitAnswer(
//                MmpiTestingProcess.Answer.byValue(
//                    env.pollAnswer.optionIds.first()
//                )
//            )
//            if (ongoingProcess!!.hasNextQuestion()) {
//                sendQuestion(
//                    bot = env.bot,
//                    userId = id,
//                    question = ongoingProcess!!.nextQuestion()
//                )
//            } else {
//                sendResult(
//                    bot = env.bot,
//                    userId = id,
//                    result = Message.TestResult(ongoingProcess!!.calculateResult().format())
//                )
//            }
//        }
    }

    override fun callbackQuery(env: CallbackQueryHandlerEnvironment) {
        if (env.callbackQuery.message?.messageId == genderPollId) {
            val gender = Gender.byValue(env.callbackQuery.data.toInt())
            ongoingProcess = MmpiTestingProcess(gender)

            sendQuestion(
                bot = env.bot,
                userId = id,
                question = ongoingProcess!!.nextQuestion()
            )
        } else {
            ongoingProcess!!.submitAnswer(
                MmpiTestingProcess.Answer.byValue(
                    env.callbackQuery.data.toInt()
                )
            )
            if (ongoingProcess!!.hasNextQuestion()) {
                sendQuestion(
                    bot = env.bot,
                    userId = id,
                    question = ongoingProcess!!.nextQuestion()
                )
            } else {
                sendResult(
                    bot = env.bot,
                    userId = id,
                    result = Message.TestResult(ongoingProcess!!.calculateResult().format())
                )
            }
        }
    }

    override fun start(env: CommandHandlerEnvironment) {
        genderPollId = sendQuestion(
            bot = env.bot,
            userId = id,
            question = createGenderQuestion()
        )
    }
}