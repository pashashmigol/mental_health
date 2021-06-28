import storage.CentralDataStorage
import telegram.BotLauncher
import telegram.LaunchMode

/**
 * starts bot locally for debugging
 * */
fun main() {
    val launchMode = LaunchMode.LOCAL
    CentralDataStorage.init(launchMode = launchMode, testingMode = true)

    BotLauncher(
        mode = launchMode,
        tokens = listOf(TESTING_TOKENS)
    ).launchBots()
}