package org.jetbrains.kotlinx.jupyter.database.containers

import org.testcontainers.containers.MSSQLServerContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.oracle.OracleContainer
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.ConcurrentHashMap

/**
 * Class responsible for tracking running database containers.
 */
class ContainerManager {
    private val supportedContainers =
        mapOf(
            ContainerId("postgres") to "postgres:16-alpine",
            ContainerId("mysql") to "mysql:5.7.34",
            ContainerId("mssql") to "mcr.microsoft.com/mssql/server:2022-CU20-ubuntu-22.04",
            ContainerId("oracle-free") to "gvenzl/oracle-free:23.4-slim-faststart",
        )

    private val runningContainers = ConcurrentHashMap<ContainerId, DatabaseContainerHandle>()

    fun isSupported(id: ContainerId): Boolean = supportedContainers.containsKey(id)

    fun start(id: ContainerId): DatabaseContainerHandle =
        runningContainers.computeIfAbsent(id) {
            when (id.container) {
                "postgres" -> {
                    val imageName = supportedContainers[id] ?: error("Could not find image for container: $id")
                    val image = DockerImageName.parse(imageName)
                    val container = PostgreSQLContainer(image)
                    container.start()
                    PostgresHandle(id, container)
                }
                "mysql" -> {
                    val imageName = supportedContainers[id] ?: error("Could not find image for container: $id")
                    val image = DockerImageName.parse(imageName)
                    val container = MySQLContainer(image)
                    container.start()
                    MySqlHandle(id, container)
                }
                "mssql" -> {
                    val imageName = supportedContainers[id] ?: error("Could not find image for container: $id")
                    val image = DockerImageName.parse(imageName)
                    val container = MSSQLServerContainer(image).acceptLicense()
                    container.start()
                    MsSqlHandle(id, container)
                }
                "oracle-free" -> {
                    val imageName = supportedContainers[id] ?: error("Could not find image for container: $id")
                    val image = DockerImageName.parse(imageName)
                    val container = OracleContainer(image)
                    container.start()
                    OracleFreeHandle(id, container)
                }
                else -> error("Unknown container i: $id")
            }
        }

    fun stop(profile: ContainerId): Boolean =
        runningContainers.remove(profile)?.let {
            it.stop()
            true
        } ?: false

    fun stopAll() {
        runningContainers.keys.toList().forEach { stop(it) }
    }
}
