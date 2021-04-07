import storage.CentralDataStorage
import telegram.LaunchMode
import telegram.launchBots

/**
 * starts bot locally for debugging
 * */
fun main() {
    val launchMode = LaunchMode.LOCAL
    CentralDataStorage.init(launchMode.rootPath)
    launchBots(mode = launchMode)
}