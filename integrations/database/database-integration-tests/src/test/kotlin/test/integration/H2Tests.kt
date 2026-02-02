package org.jetbrains.kotlinx.jupyter.database.test.integration

import io.kotest.matchers.string.shouldStartWith
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.DatabaseIntegrationTest
import kotlin.test.Test

/**
 * Test loading JDBC driver for H2 (in-memory/file) databases and that it can be used to open a H2 file.
 *
 * We should probably also test the server-mode, but that needs to be setup in the `library-integration-tests`
 * module and will require custom modifications since it doesn't look like test containers have an easy setup
 * for this. So this work has been postponed as the most critical part of this test is making sure that the
 * driver is loaded correctly.
 */
class H2Tests : DatabaseIntegrationTest() {
    @Test
    fun createConnectionToMemoryDatabase() {
        execSuccess(
            """
            val src = createDataSource(
                jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            )
            src
            """.trimIndent(),
        ).renderedValue.toString() shouldStartWith "HikariDataSource"
        execSuccess(
            """
            src.connection.use { conn ->
                val stmt = conn.createStatement()
                stmt.execute("CREATE TABLE IF NOT EXISTS test(id INT PRIMARY KEY, name VARCHAR(255))")
            }
            """.trimIndent(),
        )
    }

    @Test
    fun createConnectionToFileDatabase() {
        val path =
            this::class.java
                .getResource("/h2-example.mv.db")
                ?.toURI()
                ?.path ?: error("Database file not found")
        execSuccess(
            """
            val src = createDataSource(
                jdbcUrl = "jdbc:h2:file:${path.removeSuffix(".mv.db")}"
            )
            src
            """.trimIndent(),
        ).renderedValue.toString() shouldStartWith "HikariDataSource"
        execSuccess(
            """
            src.connection.use { conn ->
                val stmt = conn.createStatement()
                stmt.execute("SELECT * FROM test")
            }
            """.trimIndent(),
        )
    }
}
