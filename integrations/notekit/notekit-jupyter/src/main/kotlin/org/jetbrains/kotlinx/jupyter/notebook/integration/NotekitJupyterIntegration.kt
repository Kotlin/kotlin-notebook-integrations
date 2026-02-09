package org.jetbrains.kotlinx.jupyter.notebook.integration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.jetbrains.jupyter.parser.notebook.Cell
import org.jetbrains.jupyter.parser.notebook.CodeCell
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.notebook.Notekit
import org.jetbrains.kotlinx.jupyter.notebook.createNotekit

private const val NOT_LOADED_YET_MESSAGE = "Notekit integration is not loaded yet"

private var scope: CoroutineScope? = null
private var notekit: Notekit? = null

/**
 * Wraps the result of notekit block execution, providing access to the result via [asyncResult]
 * and allowing a result to be rendered in the notebook when it's ready.
 */
public class NotekitResult<T>(
    public val asyncResult: Deferred<T>,
) {
    public val isCompleted: Boolean get() = asyncResult.isCompleted
}

/**
 * Read, write, or execute the current notebook by calling Notekit methods
 */
public fun <T> notekit(block: suspend Notekit.() -> T): NotekitResult<T> {
    val myNotekit =
        requireNotNull(notekit) {
            NOT_LOADED_YET_MESSAGE
        }
    val myScope =
        requireNotNull(scope) {
            NOT_LOADED_YET_MESSAGE
        }

    return NotekitResult(myScope.async { myNotekit.block() })
}

public class NotekitJupyterIntegration : JupyterIntegration() {
    override fun Builder.onLoaded() {
        importPackage<NotekitJupyterIntegration>()
        importPackage<Notekit>()
        importPackage<Cell>()

        // We import this class separately to increase its priority against
        // same-named class from the kernel API
        import<CodeCell>()

        scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        notekit = createNotekit(notebook.commManager)

        render<NotekitResult<*>> {
            if (it.isCompleted) {
                runBlocking {
                    // This call may throw in case if operation was not successful, we're fine with that
                    it.asyncResult.await() ?: "null"
                }
            } else {
                "Notekit operation is still in progress..."
            }
        }

        onShutdown {
            notekit = null
            scope?.cancel()
            scope = null
        }
    }
}
