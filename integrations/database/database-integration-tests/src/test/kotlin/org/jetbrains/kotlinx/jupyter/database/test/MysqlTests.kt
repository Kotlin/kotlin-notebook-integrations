package org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test

import io.kotest.matchers.should
import org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test.helpers.DatabaseInfo
import org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test.helpers.DatabaseIntegrationTest
import org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test.helpers.startTestContainer
import org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test.helpers.stopTestContainer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test

/** Test loading JDBC driver for MySQL and that it can be used to connect to a MySQL database. */
class MysqlTests : DatabaseIntegrationTest() {
    companion object {
        lateinit var mysql: DatabaseInfo

        @JvmStatic
        @BeforeAll
        fun beforeClass() {
            mysql = startTestContainer("mysql")
        }

        @JvmStatic
        @AfterAll
        fun afterClass() {
            stopTestContainer("mysql")
        }
    }

    @Test
    fun createMysqlConnection() {
        execSuccess(
            """
            val src = createDataSource(
                jdbcUrl = "${mysql.jdbcUrl}",
                username = "${mysql.username}",
                password = "${mysql.password}"
            )
            src
            """.trimIndent(),
        ).renderedValue should { it.toString().startsWith("HikariDataSource") }
    }
}
