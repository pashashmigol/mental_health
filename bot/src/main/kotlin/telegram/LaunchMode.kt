package telegram

enum class LaunchMode(val rootPath: String) {
    LOCAL("bot/src/main/"),
    TESTS("src/main/"),
    APP_ENGINE("")
}