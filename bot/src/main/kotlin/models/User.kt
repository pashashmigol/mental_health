package models

import telegram.UserId

data class User(
    val id: UserId = 0,
    val name: String = "",
    val googleDriveFolderUrl: String = "",
    val googleDriveFolderId: String = "",
    val runDailyQuiz: Boolean = false
)