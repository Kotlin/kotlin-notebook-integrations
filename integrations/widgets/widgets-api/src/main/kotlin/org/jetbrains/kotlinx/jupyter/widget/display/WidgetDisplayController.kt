package org.jetbrains.kotlinx.jupyter.widget.display

import org.jetbrains.kotlinx.jupyter.protocol.api.RawMessage

/**
 * Utility interface for managing Jupyter display logic.
 * It's separated from [org.jetbrains.kotlinx.jupyter.widget.WidgetManager] because
 * it provides only custom logic needed mostly for output widget.
 */
public interface WidgetDisplayController {
    /**
     * The original Jupyter message that triggered the current execution.
     * Used as a parent for outgoing widget messages.
     */
    public val contextMessage: RawMessage?

    /**
     * Clears the output of the current cell.
     *
     * @param wait If true, wait to clear the output until a new output is available.
     */
    public fun clearOutput(wait: Boolean = false)
}
