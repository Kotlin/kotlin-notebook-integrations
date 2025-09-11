package org.jetbrains.kotlinx.jupyter.database.internal.drivers

import org.jetbrains.kotlinx.jupyter.api.Notebook
import org.jetbrains.kotlinx.jupyter.database.DataSourceDriverConfig

internal fun loadDriversIfNeeded(notebook: Notebook, jdbcUrl: String) {
    val logger = notebook.loggerFactory.getLogger("DriverLoader")
    for (loader in DataSourceDriverConfig.driverLoaders) {
        logger.info("Checking if driver should be loaded by loader $loader for $jdbcUrl")
        if (loader.shouldLoadDriver(jdbcUrl)) {
            logger.info("Driver should be loaded by loader: $loader")
            loader.loadDriver(notebook)
        }
    }
}




