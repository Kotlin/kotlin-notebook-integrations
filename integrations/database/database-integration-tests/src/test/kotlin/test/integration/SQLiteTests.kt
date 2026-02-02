package org.jetbrains.kotlinx.jupyter.database.test.integration

import io.kotest.matchers.string.shouldStartWith
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.DatabaseIntegrationTest
import kotlin.test.Test

/**
 * Test loading JDBC driver for SQLite (in-memory and file) databases.
 */
class SQLiteTests : DatabaseIntegrationTest() {
    @Test
    fun createConnectionToMemoryDatabase() {
        execSuccess(
            """
            val src = createDataSource(
                jdbcUrl = "jdbc:sqlite::memory:",
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
                .getResource("/sqlite-example.db")
                ?.toURI()
                ?.path ?: error("Database file not found")
        execSuccess(
            """
            val src = createDataSource(
                jdbcUrl = "jdbc:sqlite:$path"
            )
            src
            """.trimIndent(),
        ).renderedValue.toString() shouldStartWith "HikariDataSource"

        execSuccess(
            """
            src.connection.use { conn ->
                val stmt = conn.createStatement()
                stmt.execute("SELECT * FROM albums")
            }
            """.trimIndent(),
        )
    }
}
