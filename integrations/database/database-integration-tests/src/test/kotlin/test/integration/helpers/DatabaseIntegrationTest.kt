package org.jetbrains.kotlinx.jupyter.database.test.integration.helpers

import org.jetbrains.kotlinx.jupyter.database.gen.BuildConfig
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import org.junit.jupiter.api.BeforeEach

/**
 * Set up the Jupyter test infrastructure to support resolving normal maven dependencies,
 * and then load the database integration as a normal notebook.
 *
 * This is required to test dynamically loading database drivers.
 */
open class DatabaseIntegrationTest :
    JupyterReplTestCase(
        replProvider = StandardReplProvider.withStandardResolver(),
    ) {
    @BeforeEach
    fun setUp() {
        val libraryVersion = BuildConfig.LIBRARY_VERSION
        execSuccess(
            """
            @file:Repository("*mavenLocal")
            @file:DependsOn("org.jetbrains.kotlinx:kotlin-jupyter-database:$libraryVersion")
            """.trimIndent(),
        )
    }
}
