import storage.CentralDataStorage
import telegram.LaunchMode
import telegram.launchBots

/**
 * starts bot locally for debugging
 * */
fun main() {
    val launchMode = LaunchMode.LOCAL
    CentralDataStorage.init(rootPath = launchMode.rootPath, testingMode = true)
    launchBots(mode = launchMode, tokens = TESTING_TOKENS)
}