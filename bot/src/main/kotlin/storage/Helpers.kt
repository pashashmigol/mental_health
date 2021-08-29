package storage

import com.google.api.services.drive.model.Permission


internal fun List<List<Any>>.toRawEntries(): List<Map<String, String>> {
    val headers = first().map { it as String }
    val rows = drop(1).map { row ->
        row.map { field ->
            field.toString()
        }
    }
    return rows.map { row ->
        headers.zip(row).toMap()
    }
}

//internal fun giveAccess(folderId: String, connection: GoogleDriveConnection) {
//    val permission = Permission()
//
//    val details = Permission.PermissionDetails()
//    permission.role = "reader"
//    permission.type = "anyone"
//
//    permission.permissionDetails = listOf(details)
//
//    val result: Permission = connection.driveService.permissions()
//        .create(folderId, permission)
//        .execute()
//
//    println("result : $result")
//}

internal fun deleteFolder(folderId: String, connection: GoogleDriveConnection){
    connection.driveService
        .files()
        .delete(folderId)
        .execute()
}