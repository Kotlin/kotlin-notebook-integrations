package org.jetbrains.kotlinx.jupyter.database.internal.drivers

import org.jetbrains.kotlinx.jupyter.api.Notebook
import org.jetbrains.kotlinx.jupyter.api.dependencies.DependencyDescription
import org.jetbrains.kotlinx.jupyter.api.dependencies.ResolutionResult
import org.jetbrains.kotlinx.jupyter.util.ModifiableParentsClassLoader
import java.net.URLClassLoader
import java.sql.Driver
import java.util.ServiceLoader

/**
 * Loader for fetching drivers from an external Maven repository.
 */
internal class ExternalDependencyDriverLoader(
    // database identifiers used in JDBC urls
    override val names: List<String>,
    // dependencies to be loaded for the driver in Gradle dependency format
    private val dependenciesToLoad: List<String>,
) : DriverLoader {
    @Volatile
    private var loaded = false

    override fun shouldLoadDriver(jdbcUrl: String): Boolean {
        if (loaded) return false
        return names.any { name ->
            jdbcUrl.startsWith("jdbc:$name")
        }
    }

    override fun loadDriver(notebook: Notebook) {
        if (loaded) return
        doLoad(notebook)
    }

    @Synchronized
    private fun doLoad(notebook: Notebook) {
        if (loaded) return
        loaded = true

        val logger = notebook.loggerFactory.getLogger(this::class.java)
        logger.info("Loading JDBC driver for ${names.first()}")
        val resolver = notebook.dependencyManager.resolver
        val customizableClassLoader =
            notebook.intermediateClassLoader as? ModifiableParentsClassLoader ?: run {
                logger.warn(
                    "Can't load JDBC driver for ${names.first()}: custom class loader is not modifiable (${notebook.intermediateClassLoader})",
                )
                return
            }

        val resolutionResult =
            resolver.resolve(
                dependenciesToLoad.map { DependencyDescription(it) },
            )

        val resolvedJars =
            when (resolutionResult) {
                is ResolutionResult.Success -> {
                    resolutionResult.binaryClasspath
                }
                is ResolutionResult.Failure -> {
                    throw IllegalStateException("Failed to load JDBC driver for ${names.first()}: ${resolutionResult.message}")
                }
            }

        logger.info("Resolved JDBC driver for ${names.first()}: $resolvedJars")

        val urlClassLoader = URLClassLoader(resolvedJars.map { it.toURI().toURL() }.toTypedArray())

        // TODO: Ideally, we should add all URL Classloaders to one classloader,
        //  dedicated to our integration to avoid the pollution of "common" space
        customizableClassLoader.addParent(urlClassLoader)

        for (driver in ServiceLoader.load(Driver::class.java, urlClassLoader)) {
            logger.info("Loaded JDBC driver: ${driver.javaClass.name}")
        }
    }
}
