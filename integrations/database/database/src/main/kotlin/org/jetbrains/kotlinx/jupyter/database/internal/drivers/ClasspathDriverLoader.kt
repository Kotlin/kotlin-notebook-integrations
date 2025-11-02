package org.jetbrains.kotlinx.jupyter.database.internal.drivers

import org.jetbrains.kotlinx.jupyter.api.Notebook

/**
 * Loader representing drivers expected to already be on the classpath.
 */
internal class ClasspathDriverLoader(
    override val names: List<String>,
) : DriverLoader {
    override fun shouldLoadDriver(jdbcUrl: String): Boolean {
        return false // Always assume it is present
    }

    override fun loadDriver(notebook: Notebook) {
        // Do nothing
    }
}
