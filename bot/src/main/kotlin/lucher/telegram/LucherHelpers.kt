package lucher.telegram

import kotlinx.coroutines.delay
import lucher.LucherColor
import lucher.callbackData
import storage.CentralDataStorage.string
import telegram.Button
import telegram.UserConnection


suspend fun askUserToWaitBeforeSecondRound(
    chatId: Long,
    minutes: Int,
    userConnection: UserConnection
) {
    askUserToWait(chatId, minutes, userConnection)
    delay(minutes * 60 * 1000L)
    userConnection.cleanUp()
}

fun askUserToWait(
    chatId: Long,
    minutes: Int,
    userConnection: UserConnection
) {
    userConnection.sendMessage(
        chatId = chatId,
        text = string("lucher_timeout", minutes)
    )
}

fun allColorsChosen(answers: List<LucherColor>) = answers.size == LucherColor.values().size

fun createReplyOptions(colors: List<LucherColor>): MutableList<Button> {
    val options = mutableListOf<Button>()
    options.addAll(colors.map { it.callbackData() })
    return options
}