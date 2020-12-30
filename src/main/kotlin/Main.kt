package com.github.kotlintelegrambot.echo

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text

fun main(args: Array<String>) {

    val bot = bot {

        token = "1466693925:AAGkgMILgAg4gvs4Ct6UFlPM5T9VJx0BoIY"

        dispatch {
            text {
                bot.sendMessage(chatId = message.chat.id, text = text)
            }
        }
    }
    bot.startPolling()
}