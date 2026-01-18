package org.jetbrains.kotlinx.jupyter.widget.test.util

import org.jetbrains.kotlinx.jupyter.api.EmbeddedKernelRunMode
import org.jetbrains.kotlinx.jupyter.api.libraries.CommManager
import org.jetbrains.kotlinx.jupyter.config.DefaultKernelLoggerFactory
import org.jetbrains.kotlinx.jupyter.config.defaultRepositoriesCoordinates
import org.jetbrains.kotlinx.jupyter.libraries.LibraryResolver
import org.jetbrains.kotlinx.jupyter.libraries.createLibraryHttpUtil
import org.jetbrains.kotlinx.jupyter.repl.ReplForJupyter
import org.jetbrains.kotlinx.jupyter.repl.creating.createRepl
import org.jetbrains.kotlinx.jupyter.repl.embedded.NoOpInMemoryReplResultsHolder
import org.jetbrains.kotlinx.jupyter.testkit.ClasspathLibraryResolver
import org.jetbrains.kotlinx.jupyter.testkit.ReplProvider
import org.jetbrains.kotlinx.jupyter.testkit.ToEmptyLibraryResolver
import java.io.File

class WidgetReplProvider(
    private val commManager: CommManager,
) : ReplProvider {
    private val httpUtil = createLibraryHttpUtil(DefaultKernelLoggerFactory)

    override fun invoke(classpath: List<File>): ReplForJupyter {
        val resolver =
            run {
                var res: LibraryResolver = ClasspathLibraryResolver(httpUtil.libraryDescriptorsManager, null) { true }
                res = ToEmptyLibraryResolver(res) { it == "widgets" }
                res
            }

        return createRepl(
            httpUtil = httpUtil,
            scriptClasspath = classpath,
            kernelRunMode = EmbeddedKernelRunMode,
            mavenRepositories = defaultRepositoriesCoordinates,
            libraryResolver = resolver,
            inMemoryReplResultsHolder = NoOpInMemoryReplResultsHolder,
            commManager = commManager,
        ).apply {
            initializeWithCurrentClasspath()
        }
    }

    private fun ReplForJupyter.initializeWithCurrentClasspath() {
        eval { librariesScanner.addLibrariesFromClassLoader(currentClassLoader, this, notebook) }
    }
}
