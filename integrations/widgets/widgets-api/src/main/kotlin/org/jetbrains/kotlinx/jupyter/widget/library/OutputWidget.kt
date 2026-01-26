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
    /**
     * Factory for creating [OutputWidget] instances.
     */
    internal object Factory : DefaultWidgetFactory<OutputWidget>(outputSpec, ::OutputWidget)

    private var scopeCounter = 0
    private val displayController get() = widgetManager.displayController

    public val outputs: List<Map<String, Any?>> get() = _outputs

    /**
     * Clears the current outputs of the widget.
     * @param wait If true, wait to clear the output until new output is available.
     */
    public fun clear(wait: Boolean = false) {
        withScope {
            displayController.clearOutput(wait)
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
        val parentId = displayController.contextMessage?.id
        if (parentId != null) msgId = parentId
        try {
            action()
        } finally {
            exitScope()
        }
    }

    /**
     * Append text to the stdout stream.
     */
    public fun appendStdOut(text: String) {
        appendStreamOutput("stdout", text)
    }

    /**
     * Append text to the stderr stream.
     */
    public fun appendStdErr(text: String) {
        appendStreamOutput("stderr", text)
    }

    /**
     * Append a display object as an output.
     *
     * @param displayObject The object to display.
     */
    public fun appendDisplayData(displayObject: Any?) {
        val displayResult = displayController.render(displayObject) ?: return
        val json = displayResult.toJson()
        appendOutput(
            mapOf(
                "output_type" to "display_data",
                "data" to json["data"],
                "metadata" to json["metadata"],
            ),
        )
    }

    private fun appendStreamOutput(
        streamName: String,
        text: String,
    ) {
        appendOutput(
            mapOf(
                "output_type" to "stream",
                "name" to streamName,
                "text" to text,
            ),
        )
    }

    private fun appendOutput(output: Map<String, Any?>) {
        _outputs = _outputs + output
    }

    private fun exitScope() {
        try {
            flushStreams()
        } finally {
            scopeCounter--
            // Reset msgId only when the outermost scope finishes
            if (scopeCounter == 0) {
                msgId = ""
            }
        }
    }

    private fun flushStreams() {
        System.out.flush()
        System.err.flush()
    }
}
