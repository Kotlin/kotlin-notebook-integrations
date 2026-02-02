package org.jetbrains.kotlinx.jupyter.notebook.integration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.jetbrains.jupyter.parser.notebook.Cell
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.notebook.NotebookManipulator
import org.jetbrains.kotlinx.jupyter.notebook.createNotebookManipulator

private val scope = CoroutineScope(Dispatchers.Default)
private var manipulator: NotebookManipulator? = null

public class ManipulationResult<T>(
    private val deferred: Deferred<T>,
) {
    public val isCompleted: Boolean get() = deferred.isCompleted

    public val result: T by lazy {
        runBlocking {
            deferred.await()
        }
    }
}

public fun <T> manipulateNotebook(block: suspend NotebookManipulator.() -> T): ManipulationResult<T> =
    ManipulationResult(scope.async { manipulator!!.block() })

public class NotebookManipulatorJupyterIntegration : JupyterIntegration() {
    override fun Builder.onLoaded() {
        importPackage<NotebookManipulatorJupyterIntegration>()
        importPackage<NotebookManipulator>()
        importPackage<Cell>()

        manipulator = createNotebookManipulator(notebook.commManager)

        render<ManipulationResult<*>> {
            if (it.isCompleted) {
                it.result ?: "null"
            } else {
                "Notebook manipulation is still in progress..."
            }
        }
    }
}
