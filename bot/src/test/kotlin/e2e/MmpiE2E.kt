package e2e

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import storage.CentralDataStorage
import telegram.LaunchMode
import storage.regenerateReports


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MmpiE2E {

    @BeforeAll
    fun init() {
        CentralDataStorage.init(
            rootPath = LaunchMode.TESTS.rootPath,
            testingMode = true
        )
    }

    @Test
    fun `full cycle from saved answers`() = runBlocking {
        val lin = regenerateReports(413162911, Gender.Male)
    }
}