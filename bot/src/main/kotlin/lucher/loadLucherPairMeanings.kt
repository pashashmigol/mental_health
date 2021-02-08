package lucher

import storage.GoogleDriveConnection

fun loadLucherData(connection: GoogleDriveConnection): LucherData {
    val list: List<Map<String, String>> = connection.loadDataFromFile(
        fileId = "1yp9goGpmgNRwnFa74yKh87xIzw25czpqqWzA0gXrVG0",
        page = "'pairs'"
    )
    val meanings = list.fold(initial = mapOf()) { acc: Map<String, String>, row: Map<String, String> ->
        val pair = row["pair"]!! to row["description"]!!
        return@fold acc + pair
    }

    return LucherData(meanings = meanings)
}