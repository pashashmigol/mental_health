import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.pollAnswer
import com.github.kotlintelegrambot.entities.polls.PollType

fun main() {
    bot {
        token = "1466693925:AAGkgMILgAg4gvs4Ct6UFlPM5T9VJx0BoIY"
        dispatch {
            pollAnswer {
                println("${pollAnswer.user.firstName} has selected the option " +
                        "${pollAnswer.optionIds.lastOrNull()} in the poll ${pollAnswer.pollId}")

                WorkSpace.onAnswer(this)
            }
            command("mockTest") {
                WorkSpace.launchMockTest(this)
            }

//            command("mockTest") {
//                bot.sendPoll(
//                    chatId = message.chat.id,
//                    type = PollType.QUIZ,
//                    question = "Java or Kotlin?",
//                    options = listOf("Java", "Kotlin"),
//                    correctOptionId = 1,
//                    isAnonymous = false
//                )
//            }

            command("closedPoll") {
                bot.sendPoll(
                    chatId = message.chat.id,
                    type = PollType.QUIZ,
                    question = "${message.chat.firstName}, Foo or Bar?",
                    options = listOf("Foo", "Bar", "FooBar"),
                    correctOptionId = 1,
                    isClosed = message.chat.firstName != "Pasha",
                    explanation = "A closed quiz because I can"
                )
            }
        }
    }.startPolling()
}