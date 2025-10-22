package org.jetbrains.kotlinx.jupyter.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jupyter.kotlin.ScriptTemplateWithDisplayHelpers
import org.jetbrains.kotlinx.jupyter.api.Notebook
import org.jetbrains.kotlinx.jupyter.database.internal.SpringHikari
import org.jetbrains.kotlinx.jupyter.database.internal.drivers.loadDriversIfNeeded
import java.nio.file.Path
import javax.sql.DataSource

/**
 * Creates a data source from a Spring application file. Both properties (.properties) and yaml formats (.yml, .yaml)
 * are supported.
 *
 * @param path Path to the Spring applications property or yaml file. For relative paths, [Notebook.workingDir] will be
 * used as the base directory.
 */
fun ScriptTemplateWithDisplayHelpers.createDataSourceFromSpring(path: String): DataSource {
    val config = SpringHikari.fromFile(path)
    return createHikariDataSource(config, notebook)
}

/**
 * Creates a data source from a Spring application file. Both properties (.properties) and yaml formats (.yml, .yaml)
 * are supported.
 *
 * @param path Path to Springs application property file. For relative paths, [Notebook.workingDir] will be
 * used as the base directory.
 */
fun ScriptTemplateWithDisplayHelpers.createDataSourceFromSpring(path: Path): DataSource {
    val config = SpringHikari.fromFile(path)
    return createHikariDataSource(config, notebook)
}

/**
 * Create a simple [DataSource] using only the JDBC URL, username, and password.
 * See your database documentation for the correct format of the JDBC url.
 *
 * For more complex use cases, use [createDataSource] with a builder instead.
 *
 * Embedded databases like H2 and SQLite are also supported through the JDBC url.
 * The url looks slightly different depending on the exact use case:
 *
 * H2:
 * - File: "jdbc:h2:file:/path/to/my_database" // Absolute or relative path to the file excluding the `.mv.db` suffix.
 * - In-memory: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
 *
 * SQLite:
 * - File: "jdbc:sqlite:/path/to/my_database.db" // Absolute or relative path to the file.
 * - In-memory: "jdbc:sqlite::memory"
 */
fun ScriptTemplateWithDisplayHelpers.createDataSource(
    jdbcUrl: String,
    username: String? = null,
    password: String? = null,
): DataSource {
    return createDataSource {
        this.jdbcUrl = jdbcUrl
        username?.let { this.username = it }
        password?.let { this.password = it }
    }
}

/**
 * Create a [DataSource] using a [HikariConfig] builder.
 */
fun ScriptTemplateWithDisplayHelpers.createDataSource(configAction: HikariConfig.() -> Unit): DataSource {
    val config = HikariConfig().apply(configAction)
    return createHikariDataSource(config, notebook)
}

/**
 * Object used to configure the drivers used to create a [DataSource] connection.
 */
val Notebook.dataSourceDriverConfig: DataSourceDriverConfig
    get() = DataSourceDriverConfig

private fun createHikariDataSource(
    config: HikariConfig,
    notebook: Notebook,
): DataSource {
    loadDriversIfNeeded(notebook, config.jdbcUrl)
    return HikariDataSource(config)
}
