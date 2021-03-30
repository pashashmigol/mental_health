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

fun allColorsChosen(answers: List<LucherColor>) = answers.size == LucherColor.values().size - 1

fun showAllColors(chatId: Long, connection: UserConnection){
    LucherColor.values().forEach { color ->
        connection.sendMessageWithPicture(chatId, color)
    }
}

fun createReplyOptions(): MutableList<Button> {
    val options = mutableListOf<Button>()
    options.addAll(LucherColor.values().map { it.callbackData() })
    return options
}