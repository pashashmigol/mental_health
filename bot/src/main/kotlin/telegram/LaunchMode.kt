package telegram

enum class LaunchMode(val rootPath: String) {
    LOCAL("bot/src/main/webapp/"),
    APP_ENGINE("")
}