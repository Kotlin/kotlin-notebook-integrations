package org.jetbrains.kotlinx.jupyter.database.test

import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
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
class CreateConnectionTests: JupyterReplTestCase() {

    companion object {
        lateinit var postgres: PostgreSQLContainer<*>
        @JvmStatic
        @BeforeAll
        fun beforeClass() {
            postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
                .withInitScript("init_postgres.sql")
            postgres.start()
        }
        @JvmStatic
        @AfterAll
        fun afterClass() {
            postgres.stop()
        }
    }

    @Test
    fun connectUsingSpringApplicationFile_string() {

    }

    @Test
    fun connectUsingSpringApplicationFile_path() {

    }

    @Test
    fun connectUsingSpringApplicationFile_resource() {}


    @Test
    fun createDataSrcWithBuilder() {

    }

    @Test
    fun createSimpleDataSource() {

    }
}