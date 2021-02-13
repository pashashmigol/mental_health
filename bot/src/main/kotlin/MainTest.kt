import storage.CentralDataStorage
import telegram.LaunchMode
import telegram.launchBot

private const val TOKEN = "1417030770:AAEI89UL2hYjEuuiX55_6HLtjs6pUSeaNMI"

/**
refactor * starts bot locally for debugging
 * */
fun main() {
    val launchMode = LaunchMode.LOCAL
    CentralDataStorage.init(launchMode.rootPath)
    CentralDataStorage.reload()
    launchBot(mode = LaunchMode.LOCAL, token = TOKEN)
}