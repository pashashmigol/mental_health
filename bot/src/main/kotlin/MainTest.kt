import mmpi.CurrentQuestionsProvider

private const val TOKEN = "1417030770:AAEI89UL2hYjEuuiX55_6HLtjs6pUSeaNMI"

fun main() {
    CurrentQuestionsProvider.initGoogleSheetsProvider(rootPath = "bot/src/main/webapp/")
    launchBot(mode = LaunchMode.LOCAL, token = TOKEN)
}