private const val TOKEN = "1466693925:AAGkgMILgAg4gvs4Ct6UFlPM5T9VJx0BoIY"

fun main() {
    CurrentQuestionsProvider.initGoogleSheetsProvider(rootPath = "bot/src/main/webapp/")
    launchBot(testingMode = true, token = TOKEN)
}