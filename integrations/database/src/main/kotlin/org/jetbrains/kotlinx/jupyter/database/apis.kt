package org.jetbrains.kotlinx.jupyter.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jupyter.kotlin.ScriptTemplateWithDisplayHelpers
import org.jetbrains.kotlinx.jupyter.api.Notebook
import javax.sql.DataSource

fun ScriptTemplateWithDisplayHelpers.createDataSrcBySpringAppProperties(
    path: String
): DataSource {
    val config = SpringHikari.fromFile(path)
    return createHikariDataSource(config, notebook)
}

fun ScriptTemplateWithDisplayHelpers.createDataSrc(
    configAction: HikariConfig.() -> Unit,
): DataSource {
    val config = HikariConfig().apply(configAction)
    return createHikariDataSource(config, notebook)
}

fun ScriptTemplateWithDisplayHelpers.createDataSrc(
    jdbcUrl: String,
    username: String? = null,
    password: String? = null,
): DataSource = createDataSrc {
    this.jdbcUrl = jdbcUrl
    username?.let { this.username = it }
    password?.let { this.password = it }
}

private fun createHikariDataSource(
    config: HikariConfig,
    notebook: Notebook,
): DataSource {
    loadDriversIfNeeded(notebook, config.jdbcUrl)

    return HikariDataSource(config)
}
