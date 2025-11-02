package org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test

import io.kotest.matchers.should
import org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test.helpers.DatabaseInfo
import org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test.helpers.DatabaseIntegrationTest
import org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test.helpers.startTestContainer
import org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test.helpers.stopTestContainer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test

/** Test loading JDBC driver for Oracle and that it can be used to connect to a Oracle database. */
class OracleFreeTests : DatabaseIntegrationTest() {
    companion object {
        lateinit var oracle: DatabaseInfo

        @JvmStatic
        @BeforeAll
        fun beforeClass() {
            oracle = startTestContainer("oracle-free")
        }

        @JvmStatic
        @AfterAll
        fun afterClass() {
            stopTestContainer("oracle-free")
        }
    }

    @Test
    fun createOracleConnection() {
        execSuccess(
            """
            val src = createDataSource(
                jdbcUrl = "${oracle.jdbcUrl}",
                username = "${oracle.username}",
                password = "${oracle.password}"
            )
            src
            """.trimIndent(),
        ).renderedValue should { it.toString().startsWith("HikariDataSource") }
    }
}
