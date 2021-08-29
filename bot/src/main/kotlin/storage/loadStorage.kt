package storage

import storage.users.UserStorage
import storage.users.UsersStorageFirebase

typealias Link = String

data class Folder(val id: String, val link: String)

fun loadUserStorage(
    connection: GoogleDriveConnection
): UserStorage = UsersStorageFirebase(connection.database)

fun loadReportsStorage(
    connection: GoogleDriveConnection,
    testingMode: Boolean
): ReportStorage = ReportStorage(connection, testingMode)