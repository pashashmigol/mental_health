package telegram

import Tokens
import com.github.kotlintelegrambot.Bot

class BotsKeeper(
    val tokens: Tokens,
    val room: TelegramRoom,
    val adminBot: Bot,
    val clientBot: Bot
)
