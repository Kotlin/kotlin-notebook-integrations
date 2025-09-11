package org.jetbrains.kotlinx.jupyter.database.internal.drivers

import org.jetbrains.kotlinx.jupyter.api.Notebook

/**
 * Interface responsible for loading any relevant JDBC drivers required for the notebook to
 * connect to a database.
 */
internal interface DriverLoader {

    // List of JDBC database identifiers this loader is used for
    val names: List<String>

    /**
     * Returns `false`, if a relevant driver was not already on the classpath, and
     * needs to be downloaded and loaded into the classpath.
     */
    fun shouldLoadDriver(jdbcUrl: String): Boolean
    /**
     * Download the required driver(s) and add them to the classpath of the current notebook.
     */
    fun loadDriver(notebook: Notebook)
}