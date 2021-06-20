package models

data class User(
    val id: Long = 0,
    val name: String = "",
    val googleDriveFolder: String = "",
)