package org.jetbrains.kotlinx.jupyter.database.test.integration

import io.kotest.matchers.string.shouldStartWith
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.DatabaseInfo
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.DatabaseIntegrationTest
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.createSpringApplicationFile
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.startTestContainer
import org.jetbrains.kotlinx.jupyter.database.test.integration.helpers.stopTestContainer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.test.Test

/**
 * This class tests various ways of creating a connection to a database.
 * We use Postgres as the example database for this.
 *
 * We only do smoke-tests against other databases. See the various
 * `<DBName>Tests` classes for the details.
 *
 * Note; we do not want to test the entire Hikari functionality here, only
 * the most relevant paths for creating connections and running statements.
 */
class CreateConnectionTests : DatabaseIntegrationTest() {
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
    fun createSimpleDataSource() {
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

    @Test
    fun createSimpleDataSource_throwOnWrongCredentials() {
        execError(
            """
            val src = createDataSource(
                jdbcUrl = "${postgres.jdbcUrl}",
                username = "non-existent-user",
                password = "${postgres.password}"
            )
            src
            """.trimIndent(),
        )
    }

    @Test
    fun createDataSourceWithHikariBuilder() {
        execSuccess(
            """
            val src = createDataSource {
                jdbcUrl = "${postgres.jdbcUrl}"
                username = "${postgres.username}"
                password = "${postgres.password}"
            }
            src
            """.trimIndent(),
        ).renderedValue.toString() shouldStartWith "HikariDataSource"
    }

    @Test
    fun createDataSourceWithHikariBuilder_throwOnWrongCredentials() {
        execError(
            """
            val src = createDataSource {
                jdbcUrl = "${postgres.jdbcUrl}"
                username = "non-existent-user"
                password = "${postgres.password}"
            }
            src
            """.trimIndent(),
        )
    }

    @Test
    fun connectUsingSpringApplicationPropertiesFile_string(
        @TempDir tempDir: Path,
    ) {
        val path =
            createSpringApplicationFile(tempDir, "application.properties") {
                """
                spring.datasource.url=${postgres.jdbcUrl}
                spring.datasource.username=${postgres.username}
                spring.datasource.password=${postgres.password}
                """.trimIndent()
            }
        execSuccess(
            """
            createDataSourceFromSpring("${path.absolutePathString()}")
        """,
        ).renderedValue.toString() shouldStartWith "HikariDataSource"
    }

    @Test
    fun connectUsingSpringApplicationFile_path(
        @TempDir tempDir: Path,
    ) {
        val path =
            createSpringApplicationFile(tempDir, "application.properties") {
                """
                spring.datasource.url=${postgres.jdbcUrl}
                spring.datasource.username=${postgres.username}
                spring.datasource.password=${postgres.password}
                """.trimIndent()
            }
        execSuccess(
            """
            val path = java.nio.file.Path.of("${path.absolutePathString()}")
            createDataSourceFromSpring(path)
        """,
        ).renderedValue.toString() shouldStartWith "HikariDataSource"
    }

    @Test
    fun connectUsingSpringApplicationYamlFile_string(
        @TempDir tempDir: Path,
    ) {
        listOf("yaml", "yml").forEach { fileExt ->
            val path =
                createSpringApplicationFile(tempDir, "application.$fileExt") {
                    """
                    spring:
                        datasource:
                            url: ${postgres.jdbcUrl}
                            username: ${postgres.username}
                            password: ${postgres.password}
                    """.trimIndent()
                }
            execSuccess(
                """
                createDataSourceFromSpring("${path.absolutePathString()}")
                """.trimIndent(),
            ).renderedValue.toString() shouldStartWith "HikariDataSource"
        }
    }

    @Test
    fun connectUsingSpringApplicationYamlFile_path(
        @TempDir tempDir: Path,
    ) {
        listOf("yaml", "yml").forEach { fileExt ->
            val path =
                createSpringApplicationFile(tempDir, "application.$fileExt") {
                    """
                    spring:
                        datasource:
                            url: ${postgres.jdbcUrl}
                            username: ${postgres.username}
                            password: ${postgres.password}
                    """.trimIndent()
                }
            execSuccess(
                """
                val path = java.nio.file.Path.of("${path.absolutePathString()}")
                createDataSourceFromSpring(path)
                """.trimIndent(),
            ).renderedValue.toString() shouldStartWith "HikariDataSource"
        }
    }
}
