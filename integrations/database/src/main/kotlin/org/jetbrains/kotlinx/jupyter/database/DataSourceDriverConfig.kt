package org.jetbrains.kotlinx.jupyter.database

import org.jetbrains.kotlinx.jupyter.database.internal.drivers.ClasspathDriverLoader
import org.jetbrains.kotlinx.jupyter.database.internal.drivers.DriverLoader
import org.jetbrains.kotlinx.jupyter.database.internal.drivers.ExternalDependencyDriverLoader
import kotlin.collections.reversed

/**
 * This class is responsible for configuring the JDBC drivers for the notebook.
 *
 * A number of drivers are pre-configured by default, but additional drivers
 * can be added using the [addDriver] functions. If a driver is on the
 * class path through other means, this can be configured using [useClasspathDriver].
 *
 * User-defined drivers will take precedence over pre-configured ones.
 */
public object DataSourceDriverConfig {
    private val platformDrivers: List<DriverLoader> =
        listOf(
            ExternalDependencyDriverLoader(
                listOf("postgres", "postgresql"),
                listOf(
                    "org.postgresql:postgresql:42.7.8",
                ),
            ),
            ExternalDependencyDriverLoader(
                listOf("mysql"),
                listOf(
                    "com.mysql:mysql-connector-j:9.4.0",
                ),
            ),
            ExternalDependencyDriverLoader(
                listOf("mssql"),
                listOf(
                    "com.microsoft.sqlserver:mssql-jdbc:13.2.0.jre11",
                ),
            ),
            ExternalDependencyDriverLoader(
                listOf("oracle"),
                listOf(
                    "com.oracle.database.jdbc:ojdbc11:23.9.0.25.07",
                ),
            ),
            ExternalDependencyDriverLoader(
                listOf("h2"),
                listOf(
                    "com.h2database:h2:2.4.240",
                ),
            ),
        )
    private val userDrivers: MutableList<DriverLoader> = mutableListOf()

    // Returns a list of all driver loaders. User drivers are selected first
    internal val driverLoaders: List<DriverLoader>
        get() = userDrivers.reversed() + platformDrivers

    /**
     * Configure a single driver dependency for a target JDBC identifier.
     *
     * @param jdbcIdentifier The database identifier used in the JDBC url.
     * @param driverDependency The dependency to be loaded for the driver. It should
     *  be specified in the format `group:artifact:version`.
     */
    fun addDriver(
        jdbcIdentifier: String,
        driverDependency: String,
    ) {
        val loader = ExternalDependencyDriverLoader(listOf(jdbcIdentifier), listOf(driverDependency))
        userDrivers.add(loader)
    }

    /**
     * Configure driver dependencies for one or more target JDBC identifiers.
     *
     * @param jdbcIdentifiers The database identifier(s) used in the JDBC url.
     * @param driverDependencies One or more driver dependencies. They should
     *  be specified in the format `group:artifact:version`.
     */
    fun addDriver(
        jdbcIdentifiers: List<String>,
        driverDependencies: List<String>,
    ) {
        val loader = ExternalDependencyDriverLoader(jdbcIdentifiers, driverDependencies)
        userDrivers.add(loader)
    }

    /**
     * Instead of loading a driver from a Maven dependency, use a driver that is already present on the
     * classpath. This can either be from a manual added dependency or from project dependencies
     */
    fun useClasspathDriver(vararg jdbcIdentifier: String) {
        val loader = ClasspathDriverLoader(jdbcIdentifier.toList().filter { it.isNotBlank() })
        userDrivers.add(loader)
    }

    /**
     * Remove a driver dependency for a target JDBC identifier.
     *
     * @return `true` if the driver was removed, `false` if the driver was not found.
     */
    fun removeDriver(jdbcIdentifier: String): Boolean = userDrivers.removeIf { it.names.contains(jdbcIdentifier) }

    /**
     * Remove all user-configured drivers.
     */
    fun clearUserDrivers() {
        userDrivers.clear()
    }
}
