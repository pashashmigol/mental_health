import java.io.File

fun main() {
    CurrentQuestionsProvider.initGoogleSheetsProvider(rootPath = "bot/src/main/webapp")
    launchBot(testingMode = true, token = "1417030770:AAEI89UL2hYjEuuiX55_6HLtjs6pUSeaNMI")
}