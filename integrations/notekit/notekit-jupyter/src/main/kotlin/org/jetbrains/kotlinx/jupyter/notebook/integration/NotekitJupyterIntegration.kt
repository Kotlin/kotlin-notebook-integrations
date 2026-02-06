package org.jetbrains.kotlinx.jupyter.notebook.integration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.jetbrains.jupyter.parser.notebook.Cell
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.notebook.Notekit
import org.jetbrains.kotlinx.jupyter.notebook.createNotekit

private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
private var notekit: Notekit? = null

public class NotekitResult<T>(
    private val deferred: Deferred<T>,
) {
    public val isCompleted: Boolean get() = deferred.isCompleted

    public val asyncResult: Deferred<T> get() = deferred

    /**
     * If called in the cell where the operation is initiated, may block infinitely.
     * Use this method with care.
     */
    public val result: T by lazy {
        runBlocking {
            deferred.await()
        }
    }
}

public fun <T> notekit(block: suspend Notekit.() -> T): NotekitResult<T> {
    val myNotekit =
        requireNotNull(notekit) {
            "Notekit integration is not loaded yet"
        }
    return NotekitResult(scope.async { myNotekit.block() })
}

public class NotekitJupyterIntegration : JupyterIntegration() {
    override fun Builder.onLoaded() {
        importPackage<NotekitJupyterIntegration>()
        importPackage<Notekit>()
        importPackage<Cell>()

        notekit = createNotekit(notebook.commManager)

        render<NotekitResult<*>> {
            if (it.isCompleted) {
                // This call may throw in case if operation was not successful, we're fine with that
                it.result ?: "null"
            } else {
                "Notekit operation is still in progress..."
            }
        }

        onShutdown {
            notekit = null
            scope.cancel()
        }
    }
}
