package telegram

enum class LaunchMode(val rootPath: String) {

    LOCAL("bot/src/main/"),
    TESTS("src/main/"),
    APP_ENGINE("");

    companion object {
        var current: LaunchMode = TESTS
        set(value) {
            if(field != TESTS){
                throw IllegalStateException("'current' is set already!")
            }
            field = value
        }
    }
}