package org.jetbrains.kotlinx.jupyter.database.internal

import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.database.DataSourceDriverConfig

class DatabaseJupyterIntegration: JupyterIntegration() {
    override fun Builder.onLoaded() {
        importPackage<DataSourceDriverConfig>()
    }
}