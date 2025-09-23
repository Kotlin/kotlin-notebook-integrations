package org.jetbrains.kotlinx.jupyter.database.internal

import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration

@Suppress("unused")
class DatabaseJupyterIntegration: JupyterIntegration() {
    override fun Builder.onLoaded() {
        import(
            "java.sql.*",
            "javax.sql.*",
            "org.jetbrains.kotlinx.jupyter.database.*",
        )
    }
}
