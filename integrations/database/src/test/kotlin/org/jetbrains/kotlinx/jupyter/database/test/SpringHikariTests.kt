package org.jetbrains.kotlinx.jupyter.database.test

import junit.framework.TestCase.assertFalse
import org.jetbrains.kotlinx.jupyter.database.internal.SpringHikari
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * This class is responsible for testing the conversion of a spring application file
 * into a [com.zaxxer.hikari.HikariDataSource] object.
 */
class SpringHikariTests: JupyterReplTestCase() {

    @Test
    fun throwsOnUnsupportedFileEndings() {
        val file= createTestFile("test.txt")
        assertFailsWith<IllegalArgumentException> {
            SpringHikari.fromFile(file)
        }
    }

    @Test
    fun throwsOnNonExistentFile() {
        val file= Path.of("non-existent-file")
        assertFalse(file.exists())
        assertFailsWith<IllegalArgumentException> {
            SpringHikari.fromFile(file)
        }
    }

    // Java Properties parser is very lenient when parsing files and only really
    // throws on malformed Unicode sequences, which the Kotlin compiler rejects
    // So we only test Yaml parse errors here
    @Test
    fun throwsOnYamlParseErrors() {
        val file= createTestFile("test.yaml")
        file.writeText("""
            this is not a yaml file
        """.trimIndent())
        assertFailsWith<IllegalArgumentException> {
            SpringHikari.fromFile(file)
        }
    }

    @Test
    fun parseYamlFile() {
        val file= createTestFile("test.yaml")
        file.writeText("""
            spring:
              datasource:
                url: jdbc:postgresql://localhost:5432/postgres
                username: postgres
                password: 
        """.trimIndent())
    }

    @Test
    fun parsePropertiesFile() {

    }

    @Test
    fun ignoreUnknownProperties() {

    }

    private fun createTestFile(fileType: String): Path {
        val temp = kotlin.io.path.createTempFile(suffix = ".$fileType")
        temp.toFile().deleteOnExit()
        return temp
    }
}