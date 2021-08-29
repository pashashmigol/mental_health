import io.ktor.util.*
import telegram.BotLauncher
import telegram.LaunchMode

/**
 * starts bot locally for debugging
 * */
@InternalAPI
fun main() {
    LaunchMode.current = LaunchMode.LOCAL

    BotLauncher(
        tokens = listOf(TESTING_TOKENS)
    ).launchBots()
}