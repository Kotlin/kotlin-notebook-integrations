package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.protocol.api.id
import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetFactory
import org.jetbrains.kotlinx.jupyter.widget.model.createAndRegisterWidget

public fun WidgetManager.output(setup: OutputWidget.() -> Unit = {}): OutputWidget =
    createAndRegisterWidget(OutputWidget.Factory).apply(setup)

/**
 * A widget that can capture and display standard output, error, and rich results.
 * Extends the generated [OutputWidgetBase] to add helper methods.
 */
public class OutputWidget internal constructor(
    widgetManager: WidgetManager,
    fromFrontend: Boolean,
) : OutputWidgetBase(widgetManager, fromFrontend) {
    private var scopeCounter = 0

    /**
     * Factory for creating [OutputWidget] instances.
     */
    internal object Factory : DefaultWidgetFactory<OutputWidget>(outputSpec, ::OutputWidget)

    /**
     * Clears the current output of the widget.
     * @param wait If true, wait to clear the output until new output is available.
     */
    public fun clearOutput(wait: Boolean = false) {
        withScope {
            widgetManager.displayController.clearOutput(wait)
        }
    }

    /**
     * Executes the [action] and captures any output produced by it into this widget.
     * This works by temporarily setting [msgId] to the current context message's ID.
     *
     * IMPORTANT: This only works for output produced via standard Jupyter messaging
     * (e.g., `println`, `display`).
     */
    public fun withScope(action: () -> Unit) {
        scopeCounter++
        // Use the ID of the message that triggered the current execution
        val parentId = widgetManager.displayController.contextMessage?.id
        if (parentId != null) msgId = parentId
        try {
            action()
        } finally {
            scopeCounter--
            // Reset msgId only when the outermost scope finishes
            if (scopeCounter == 0) {
                msgId = ""
            }
        }
    }
}
