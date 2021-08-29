package lucher.telegram

import kotlinx.coroutines.delay
import lucher.LucherColor
import lucher.callbackData
import storage.R
import telegram.Button
import telegram.MessageId
import telegram.SessionState
import telegram.UserConnection


suspend fun askUserToWaitBeforeSecondRound(
    chatId: Long,
    minutes: Int,
    sessionState: SessionState,
    userConnection: UserConnection
) {
    askUserToWait(chatId, minutes, userConnection)
        .apply { sessionState.addMessageId(this) }

    delay(minutes * 60 * 1000L)
    userConnection.cleanUp(chatId, sessionState.messageIds)
}

fun askUserToWait(
    chatId: Long,
    minutes: Int,
    userConnection: UserConnection
): MessageId {
    return userConnection.sendMessage(
        chatId = chatId,
        text = R.string("lucher_timeout", minutes)
    )
}

fun allColorsChosen(answers: List<LucherColor>) = answers.size == LucherColor.values().size

fun createReplyOptions(colors: List<LucherColor>): MutableList<Button> {
    val options = mutableListOf<Button>()
    options.addAll(colors.map { it.callbackData() })
    return options
}