package org.jetbrains.kotlinx.jupyter.database

import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration

class DatabaseJupyterIntegration: JupyterIntegration() {
    override fun Builder.onLoaded() {
        importPackage<DatabaseJupyterIntegration>()
    }
}
