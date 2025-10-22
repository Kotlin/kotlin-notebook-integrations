package org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test

import io.kotest.matchers.should
import org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test.helpers.DatabaseInfo
import org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test.helpers.DatabaseIntegrationTest
import org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test.helpers.startTestContainer
import org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test.helpers.stopTestContainer
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
        ).renderedValue should { it.toString().startsWith("HikariDataSource") }
    }
}
