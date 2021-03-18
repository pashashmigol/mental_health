import storage.CentralDataStorage
import telegram.LaunchMode
import telegram.launchBots

private const val TOKEN = "1417030770:AAEI89UL2hYjEuuiX55_6HLtjs6pUSeaNMI"

/**
 * starts bot locally for debugging
 * */
fun main() {
    val launchMode = LaunchMode.LOCAL
    CentralDataStorage.init(launchMode.rootPath)
    CentralDataStorage.reload()
    launchBots(mode = launchMode)
}