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
fun ScriptTemplateWithDisplayHelpers.createDataSourceFromSpring(
    path: String
): DataSource {
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
fun ScriptTemplateWithDisplayHelpers.createDataSourceFromSpring(
    path: Path
): DataSource {
    val config = SpringHikari.fromFile(path)
    return createHikariDataSource(config, notebook)
}

/**
 * Create a simple [DataSource] using only the JDBC URL, username, and password.
 * For more complex use cases, use [createDataSource] with a builder instead.
 */
fun ScriptTemplateWithDisplayHelpers.createDataSource(
    jdbcUrl: String,
    username: String? = null,
    password: String? = null,
): DataSource = createDataSource {
    this.jdbcUrl = jdbcUrl
    username?.let { this.username = it }
    password?.let { this.password = it }
}

/**
 * Create a [DataSource] using a [HikariConfig] builder.
 */
fun ScriptTemplateWithDisplayHelpers.createDataSource(
    configAction: HikariConfig.() -> Unit,
): DataSource {
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
