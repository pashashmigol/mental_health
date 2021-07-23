package telegram

import Tokens
import com.github.kotlintelegrambot.Bot
import io.ktor.util.*

@InternalAPI
class BotsKeeper(
    val tokens: Tokens,
    val room: TelegramRoom,
    val adminBot: Bot,
    val clientBot: Bot
)
