package mmpi.telegram

import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import mmpi.MmpiProcess
import mmpi.Type
import storage.CentralDataStorage
import telegram.OnEnded

class MmpiTestingSession(
    override val id: Long,
    override val onEndedCallback: OnEnded
) : MmpiSession(id, Type.Mmpi377, onEndedCallback) {

    override fun sendNextQuestion(
        env: CommandHandlerEnvironment,
        messageId: Long,
        ongoingProcess: MmpiProcess
    ) {
        repeat(CentralDataStorage.mmpi566Data.questionsForMen.size) {
            onAnswer?.invoke("0")
        }
    }
}