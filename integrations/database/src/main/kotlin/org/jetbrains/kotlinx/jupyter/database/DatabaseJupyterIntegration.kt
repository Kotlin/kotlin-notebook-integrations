package org.jetbrains.kotlinx.jupyter.database

import org.jetbrains.kotlinx.jupyter.api.declare
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration

class DatabaseJupyterIntegration: JupyterIntegration() {
    override fun Builder.onLoaded() {
        importPackage<DatabaseJupyterIntegration>()

        updateVariable<DataSourceStub> { value, kProperty ->
            val varName = "___${kProperty.name}"
            declare(varName to value.createDataSource())
            varName
        }
    }
}
