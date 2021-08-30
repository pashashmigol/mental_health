package storage

import models.TypeOfTest
import models.User
import Result

interface ReportStorage {
    fun saveLucher(
        user: User,
        bytes: ByteArray,
    ): Result<Folder> {
        throw NotImplementedError()
    }

    fun saveMmpi(
        user: User,
        bytes: ByteArray,
        typeOfTest: TypeOfTest
    ): Result<Folder>{
        throw NotImplementedError()
    }

    fun giveAccess(folderId: String){
        throw NotImplementedError()
    }
    fun createUserFolder(userName: String): Result<Folder>{
        throw NotImplementedError()
    }
}