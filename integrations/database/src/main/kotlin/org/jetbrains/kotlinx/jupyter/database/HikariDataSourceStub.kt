package org.jetbrains.kotlinx.jupyter.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.kotlinx.jupyter.api.Notebook
import javax.sql.DataSource

class HikariDataSourceStub(
    private val config: HikariConfig,
    private val notebook: Notebook,
) : DataSourceStub {
    override fun createDataSource(): DataSource {
        loadDriversIfNeeded(notebook, config.jdbcUrl)

        return HikariDataSource(config)
    }
}
