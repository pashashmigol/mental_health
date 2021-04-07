package mmpi.telegram

import mmpi.MmpiProcess
import models.Type
import storage.CentralDataStorage
import telegram.OnEnded
import telegram.UserConnection

class MmpiTestingSession(
    override val id: Long,
    override val clientConnection: UserConnection,
    override val adminConnection: UserConnection,
    override val onEndedCallback: OnEnded
) : MmpiSession(id, Type.Mmpi377, clientConnection, adminConnection, onEndedCallback) {

    override fun sendNextQuestion(
        ongoingProcess: MmpiProcess,
        userConnection: UserConnection
    ): Pair<Long, Int> {
        repeat(CentralDataStorage.mmpi566Data.questionsForMen.size) {
            onAnswer?.invoke(it.toLong(), MmpiProcess.Answer.Agree.name)
        }
        return Pair(0, 0)
    }
}