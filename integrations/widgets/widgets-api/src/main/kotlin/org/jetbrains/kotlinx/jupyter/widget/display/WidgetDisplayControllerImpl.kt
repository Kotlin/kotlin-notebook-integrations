package org.jetbrains.kotlinx.jupyter.widget.display

import org.jetbrains.kotlinx.jupyter.api.DisplayResult
import org.jetbrains.kotlinx.jupyter.api.Notebook
import org.jetbrains.kotlinx.jupyter.api.outputs.clearOutput
import org.jetbrains.kotlinx.jupyter.api.outputs.render
import org.jetbrains.kotlinx.jupyter.protocol.api.RawMessage

internal class WidgetDisplayControllerImpl(
    private val notebook: Notebook,
) : WidgetDisplayController {
    override val contextMessage: RawMessage?
        get() = notebook.commManager.contextMessage

    override fun clearOutput(wait: Boolean) {
        notebook.clearOutput(wait)
    }

    override fun render(displayObject: Any?): DisplayResult? = notebook.render(displayObject)
}
