package telegram

import Tokens
import io.ktor.util.*


@InternalAPI
class BotLauncher(
    val mode: LaunchMode,
    val tokens: List<Tokens>
) {
    fun launchBots(): List<BotsKeeper> {
        return tokens.map {
            var botsKeeper: BotsKeeper? = null

            val adminBot = launchAdminBot(mode, it.ADMIN)

            val (clientBot, telegramRoom) = launchClientBot(
                mode, it.ADMIN_ID, it.CLIENT
            ) { botsKeeper!! }

            botsKeeper = BotsKeeper(
                tokens = it,
                adminBot = adminBot,
                clientBot = clientBot,
                room = telegramRoom
            )
            botsKeeper
        }
    }
}