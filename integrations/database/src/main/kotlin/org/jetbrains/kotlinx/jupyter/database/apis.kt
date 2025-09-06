package org.jetbrains.kotlinx.jupyter.database

import com.zaxxer.hikari.HikariConfig
import jupyter.kotlin.ScriptTemplateWithDisplayHelpers

fun ScriptTemplateWithDisplayHelpers.createDataSrcBySpringAppProperties(
    path: String
): DataSourceStub {
    val config = SpringHikari.fromFile(path)
    return HikariDataSourceStub(config, notebook)
}

fun ScriptTemplateWithDisplayHelpers.createDataSrc(
    configAction: HikariConfig.() -> Unit,
): DataSourceStub {
    val config = HikariConfig().apply(configAction)
    return HikariDataSourceStub(config, notebook)
}

fun ScriptTemplateWithDisplayHelpers.createDataSrc(
    jdbcUrl: String,
    username: String? = null,
    password: String? = null,
): DataSourceStub = createDataSrc {
    this.jdbcUrl = jdbcUrl
    username?.let { this.username = it }
    password?.let { this.password = it }
}
