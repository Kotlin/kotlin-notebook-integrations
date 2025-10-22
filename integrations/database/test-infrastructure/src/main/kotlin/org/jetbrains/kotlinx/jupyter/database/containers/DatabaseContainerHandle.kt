package org.jetbrains.kotlinx.jupyter.database.containers

import kotlinx.serialization.Serializable
import org.testcontainers.containers.MSSQLServerContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.oracle.OracleContainer

/** Unique identifier for a container. */
@JvmInline
value class ContainerId(
    val container: String,
)

/** Wrapper for running container information. Required by tests to be able to connect to the running database. */
@Serializable
data class DatabaseInfo(
    val type: String,
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val host: String,
    val port: Int,
)

/** Interface for describing and controlling a running container with a given database. */
sealed interface DatabaseContainerHandle {
    val profile: ContainerId

    fun info(): DatabaseInfo

    fun stop()
}

class PostgresHandle(
    override val profile: ContainerId,
    private val container: PostgreSQLContainer<*>,
) : DatabaseContainerHandle {
    override fun info() =
        DatabaseInfo(
            type = "postgres",
            jdbcUrl = container.jdbcUrl,
            username = container.username,
            password = container.password,
            host = container.host,
            port = container.firstMappedPort,
        )

    override fun stop() = container.stop()
}

class MySqlHandle(
    override val profile: ContainerId,
    private val container: MySQLContainer<*>,
) : DatabaseContainerHandle {
    override fun info() =
        DatabaseInfo(
            type = "mysql",
            jdbcUrl = container.jdbcUrl,
            username = container.username,
            password = container.password,
            host = container.host,
            port = container.firstMappedPort,
        )

    override fun stop() = container.stop()
}

class MsSqlHandle(
    override val profile: ContainerId,
    private val container: MSSQLServerContainer<*>,
) : DatabaseContainerHandle {
    override fun info() =
        DatabaseInfo(
            type = "mssql",
            jdbcUrl = container.jdbcUrl,
            username = container.username,
            password = container.password,
            host = container.host,
            port = container.firstMappedPort,
        )

    override fun stop() = container.stop()
}

class OracleFreeHandle(
    override val profile: ContainerId,
    private val container: OracleContainer,
) : DatabaseContainerHandle {
    override fun info() =
        DatabaseInfo(
            type = "oracle-free",
            jdbcUrl = container.jdbcUrl,
            username = container.username,
            password = container.password,
            host = container.host,
            port = container.firstMappedPort,
        )

    override fun stop() = container.stop()
}
