package storage

import lucher.LucherData
import lucher.loadLucherData
import mmpi.MmpiData
import mmpi.loadMmpiData

object CentralDataStorage {
    val lucherData get() = lucher
    val mmpiData get() = mmpi

    private lateinit var lucher: LucherData
    private lateinit var mmpi: MmpiData

    fun reload(rootPath: String) {
        val connection = GoogleDriveConnection(rootPath)

        lucher = loadLucherData(connection)
        mmpi = loadMmpiData(connection)
    }
}