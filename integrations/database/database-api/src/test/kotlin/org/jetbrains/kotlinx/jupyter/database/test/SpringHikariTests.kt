package org.jetbrains.kotlinx.jupyter.database.test

import org.jetbrains.kotlinx.jupyter.database.internal.SpringHikari
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

/**
 * This class is responsible for testing the conversion of a spring application file
 * into a [com.zaxxer.hikari.HikariDataSource] object.
 */
class SpringHikariTests : JupyterReplTestCase() {
    @Test
    fun throwsOnUnsupportedFileEndings() {
        val file = createTestFile("test.txt")
        assertFailsWith<IllegalArgumentException> {
            SpringHikari.fromFile(file)
        }
    }

    @Test
    fun throwsOnNonExistentFile() {
        val file = Path.of("non-existent-file")
        assertFalse(file.exists())
        assertFailsWith<IllegalArgumentException> {
            SpringHikari.fromFile(file)
        }
    }

    // Java Properties parser is very lenient when parsing files and only really
    // throws on malformed Unicode sequences, which the Kotlin compiler rejects.
    // So we only test Yaml parse errors here.
    @Test
    fun throwsOnYamlParseErrors() {
        val file = createTestFile("test.yaml")
        file.writeText(
            """
            this is not a yaml file
            """.trimIndent(),
        )
        assertFailsWith<IllegalArgumentException> {
            SpringHikari.fromFile(file)
        }
    }

    @Test
    fun parseYamlFile() {
        val file = createTestFile("test.yaml")
        file.writeText(
            """
            spring:
              datasource:
                url: jdbc:postgresql://localhost:5432/postgres
                username: postgres
                password: 
            """.trimIndent(),
        )

        val config = SpringHikari.fromFile(file)
        assertEquals("jdbc:postgresql://localhost:5432/postgres", config.jdbcUrl)
        assertEquals("postgres", config.username)
        assertEquals(null, config.password)
    }

    @Test
    fun parsePropertiesFile() {
        val file = createTestFile("test.properties")
        file.writeText(
            """
            spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
            spring.datasource.username=postgres
            """.trimIndent(),
        )

        val config = SpringHikari.fromFile(file)
        assertEquals("jdbc:postgresql://localhost:5432/postgres", config.jdbcUrl)
        assertEquals("postgres", config.username)
        assertEquals(null, config.password)
    }

    @Test
    fun ignoreUnknownProperties() {
        val file = createTestFile("test.properties")
        file.writeText(
            """
            spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
            spring.datasource.username=postgres
            spring.datasource.foo=bar
            """.trimIndent(),
        )

        val config = SpringHikari.fromFile(file)
        assertEquals("jdbc:postgresql://localhost:5432/postgres", config.jdbcUrl)
        assertEquals("postgres", config.username)
        assertEquals(null, config.password)
    }

    @Test
    fun resolveSystemPropertyVariable() {
        System.setProperty("__JUPYTER_TEST_PROP", "hello")
        try {
            val file = createTestFile("test.properties")
            file.writeText(
                $$"""
                spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
                spring.datasource.username=${__JUPYTER_TEST_PROP}
                """.trimIndent(),
            )
            val config = SpringHikari.fromFile(file)
            assertEquals("hello", config.username)
        } finally {
            System.clearProperty("POSTGRES_USERNAME")
        }
    }

    @Test
    fun resolveSystemPropertyWithDefault() {
        val file = createTestFile("test.properties")
        file.writeText(
            $$"""
            spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
            spring.datasource.username=${__JUPYTER_TEST_PROP_NOT_EXISTING:defaultUsername}
            """.trimIndent(),
        )
        val config = SpringHikari.fromFile(file)
        assertEquals("defaultUsername", config.username)
    }

    @Test
    fun defaultValue_splitByFirstSeparator() {
        val file = createTestFile("test.properties")
        file.writeText(
            $$"""
            spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
            spring.datasource.username=${__JUPYTER_TEST_PROP_NOT_EXISTING:default:username}
            """.trimIndent(),
        )
        val config = SpringHikari.fromFile(file)
        assertEquals("default:username", config.username)
    }

    @Test
    fun parsePropertiesFile_jdbcUrlAlias() {
        val file = createTestFile("test.properties")
        file.writeText(
            """
            spring.datasource.jdbc-url=jdbc:postgresql://localhost:5432/postgres
            spring.datasource.username=postgres
            """.trimIndent(),
        )
        val config = SpringHikari.fromFile(file)
        assertEquals("jdbc:postgresql://localhost:5432/postgres", config.jdbcUrl)
        assertEquals("postgres", config.username)
    }

    @Test
    fun parsePropertiesFile_userAlias() {
        val file = createTestFile("test.properties")
        file.writeText(
            """
            spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
            spring.datasource.user=admin
            """.trimIndent(),
        )
        val config = SpringHikari.fromFile(file)
        assertEquals("admin", config.username)
    }

    @Test
    fun parsePropertiesFile_hikariMaximumPoolSize() {
        val file = createTestFile("test.properties")
        file.writeText(
            """
            spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
            spring.datasource.hikari.maximum-pool-size=20
            """.trimIndent(),
        )
        val config = SpringHikari.fromFile(file)
        assertEquals(20, config.maximumPoolSize)
    }

    @Test
    fun parsePropertiesFile_hikariAutoCommit_boolean() {
        val file = createTestFile("test.properties")
        file.writeText(
            """
            spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
            spring.datasource.hikari.auto-commit=false
            """.trimIndent(),
        )
        val config = SpringHikari.fromFile(file)
        assertEquals(false, config.isAutoCommit)
    }

    @Test
    fun parsePropertiesFile_hikariConnectionTimeout_milliseconds() {
        val file = createTestFile("test.properties")
        file.writeText(
            """
            spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
            spring.datasource.hikari.connection-timeout=5000
            """.trimIndent(),
        )
        val config = SpringHikari.fromFile(file)
        assertEquals(5000L, config.connectionTimeout)
    }

    @Test
    fun parsePropertiesFile_hikariConnectionTimeout_durationSeconds() {
        val file = createTestFile("test.properties")
        file.writeText(
            """
            spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
            spring.datasource.hikari.connection-timeout=5s
            """.trimIndent(),
        )
        val config = SpringHikari.fromFile(file)
        assertEquals(5_000L, config.connectionTimeout)
    }

    @Test
    fun parsePropertiesFile_hikariConnectionTimeout_durationMinutes() {
        val file = createTestFile("test.properties")
        file.writeText(
            """
            spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
            spring.datasource.hikari.connection-timeout=2m
            """.trimIndent(),
        )
        val config = SpringHikari.fromFile(file)
        assertEquals(120_000L, config.connectionTimeout)
    }

    @Test
    fun parsePropertiesFile_dataSourceProperties_springStyle() {
        val file = createTestFile("test.properties")
        file.writeText(
            """
            spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
            spring.datasource.properties.socketTimeout=30
            """.trimIndent(),
        )
        val config = SpringHikari.fromFile(file)
        assertEquals("30", config.dataSourceProperties.getProperty("socketTimeout"))
    }

    @Test
    fun parsePropertiesFile_dataSourceProperties_hikariStyle() {
        val file = createTestFile("test.properties")
        file.writeText(
            """
            spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
            spring.datasource.hikari.data-source-properties.socketTimeout=60
            """.trimIndent(),
        )
        val config = SpringHikari.fromFile(file)
        assertEquals("60", config.dataSourceProperties.getProperty("socketTimeout"))
    }

    @Test
    fun parseYamlFile_hikariMaximumPoolSize() {
        val file = createTestFile("test.yaml")
        file.writeText(
            """
            spring:
              datasource:
                url: jdbc:postgresql://localhost:5432/postgres
                hikari:
                  maximum-pool-size: 15
            """.trimIndent(),
        )
        val config = SpringHikari.fromFile(file)
        assertEquals(15, config.maximumPoolSize)
    }

    private fun createTestFile(fileType: String): Path {
        val temp = kotlin.io.path.createTempFile(suffix = ".$fileType")
        temp.toFile().deleteOnExit()
        return temp
    }
}
