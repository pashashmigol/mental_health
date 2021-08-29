package storage

import java.text.MessageFormat
import java.util.*

object R {
    private val messages: ResourceBundle = ResourceBundle.getBundle("Messages")
    private val locale = Locale("ru", "ru")

    fun string(key: String, vararg parameters: Any): String {
        return MessageFormat(messages.getString(key), locale).format(parameters)
    }

    fun string(key: String): String {
        return messages.getString(key)
    }
}