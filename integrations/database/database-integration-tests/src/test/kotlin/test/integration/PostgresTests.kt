package org.jetbrains.kotlinx.jupyter.database.test.integration

import io.kotest.matchers.string.shouldStartWith
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.DatabaseInfo
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.DatabaseIntegrationTest
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.startTestContainer
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.stopTestContainer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test

/** Test loading JDBC driver for Postgres and that it can be used to connect to a Postgres database. */
class PostgresTests : DatabaseIntegrationTest() {
    companion object {
        lateinit var postgres: DatabaseInfo

        @JvmStatic
        @BeforeAll
        fun beforeClass() {
            postgres = startTestContainer("postgres")
        }

        @JvmStatic
        @AfterAll
        fun afterClass() {
            stopTestContainer("postgres")
        }
    }

    @Test
    fun createPostgresConnection() {
        execSuccess(
            """
            val src = createDataSource(
                jdbcUrl = "${postgres.jdbcUrl}",
                username = "${postgres.username}",
                password = "${postgres.password}"
            )
            src
            """.trimIndent(),
        ).renderedValue.toString() shouldStartWith "HikariDataSource"
    }
}
