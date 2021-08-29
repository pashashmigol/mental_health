package storage.users

import models.User
import Result
import lucher.LucherAnswersContainer
import lucher.LucherResult
import lucher.report.pdfReportLucher
import mmpi.MmpiAnswersContainer
import mmpi.MmpiProcess
import mmpi.report.pdfReportMmpi
import models.Question
import models.TypeOfTest
import storage.*
import storage.deleteFolder

suspend fun createUser(
    userId: Long,
    userName: String,
    reportStorage: ReportStorage,
    userStorage: UserStorage,
): Result<Unit> {

    val folder = reportStorage.createUserFolder(userName)
        .dealWithError { return it }

    reportStorage.giveAccess(folder.id)

    val user = User(
        id = userId,
        name = userName,
        googleDriveFolderUrl = folder.link,
        googleDriveFolderId = folder.id,
        runDailyQuiz = false
    )
    return userStorage.saveUser(user)
}

suspend fun deleteUser(
    user: User,
    userStorage: UserStorage,
    connection: GoogleDriveConnection
): Result<Unit> {
    deleteFolder(user.googleDriveFolderId, connection)
    return userStorage.clearUser(user)
}

suspend fun saveMmpi(
    user: User,
    typeOfTest: TypeOfTest,
    userStorage: UserStorage,
    reportStorage: ReportStorage,
    result: MmpiProcess.Result,
    questions: List<Question>,
    answers: MmpiAnswersContainer,
    saveAnswers: Boolean
): Result<Folder> {
    if (saveAnswers) {
        userStorage.saveMmpiAnswers(answers).dealWithError {
            return it
        }
    }
    val pdfStr = pdfReportMmpi(
        questions = questions,
        answers = answers,
        result = result
    )
    return reportStorage.saveMmpi(user, pdfStr, typeOfTest)
}

suspend fun saveLucher(
    user: User,
    answers: LucherAnswersContainer,
    userStorage: UserStorage,
    reportStorage: ReportStorage,
    result: LucherResult,
    saveAnswers: Boolean
): Result<Folder> {
    if (saveAnswers) {
        userStorage.saveLucherAnswers(answers).dealWithError {
            return it
        }
    }

    val bytes = pdfReportLucher(
        answers = answers,
        result = result,
    )
    return reportStorage.saveLucher(user, bytes)
}