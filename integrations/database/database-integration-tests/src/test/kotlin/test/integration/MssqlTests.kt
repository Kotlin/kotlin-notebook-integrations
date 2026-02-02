package org.jetbrains.kotlinx.jupyter.database.test.integration

import io.kotest.matchers.string.shouldStartWith
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.DatabaseInfo
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.DatabaseIntegrationTest
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.startTestContainer
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.stopTestContainer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test

/** Test loading JDBC driver for MSSQL and that it can be used to connect to a MSSQL database. */
class MssqlTests : DatabaseIntegrationTest() {
    companion object {
        lateinit var mssql: DatabaseInfo

        @JvmStatic
        @BeforeAll
        fun beforeClass() {
            mssql = startTestContainer("mssql")
        }

        @JvmStatic
        @AfterAll
        fun afterClass() {
            stopTestContainer("mssql")
        }
    }

    @Test
    fun createMssqlConnection() {
        execSuccess(
            """
            val src = createDataSource(
                jdbcUrl = "${mssql.jdbcUrl}",
                username = "${mssql.username}",
                password = "${mssql.password}"
            )
            src
            """.trimIndent(),
        ).renderedValue.toString() shouldStartWith "HikariDataSource"
    }
}
