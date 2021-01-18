import mmpi.CurrentQuestionsProvider
import telegram.LaunchMode
import telegram.launchBot

private const val TOKEN = "1417030770:AAEI89UL2hYjEuuiX55_6HLtjs6pUSeaNMI"

/**
refactor * starts bot locally for debugging
 * */
fun main() {
    CurrentQuestionsProvider.initGoogleSheetsProvider(rootPath = "bot/src/main/webapp/")
    launchBot(mode = LaunchMode.LOCAL, token = TOKEN)
}