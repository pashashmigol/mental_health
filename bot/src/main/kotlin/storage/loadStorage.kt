package storage

import storage.users.*

typealias Link = String

data class Folder(val id: String, val link: String)

fun loadUserStorage(
    connection: GoogleDriveConnection
): UserStorageFirebase = UserStorageFirebase(connection.database)

fun loadSessionsStorage(
    connection: GoogleDriveConnection
): SessionStorage = SessionStorageFirebase(connection.database)

fun loadAnswersStorage(
    connection: GoogleDriveConnection
): AnswerStorage = AnswerStorageFirebase(connection.database)

fun loadReportsStorage(
    connection: GoogleDriveConnection,
    testingMode: Boolean
): GoogleDriveReportStorage = GoogleDriveReportStorage(connection, testingMode)