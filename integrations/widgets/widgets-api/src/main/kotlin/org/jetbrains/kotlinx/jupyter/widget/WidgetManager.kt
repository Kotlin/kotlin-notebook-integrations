package org.jetbrains.kotlinx.jupyter.widget

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.jetbrains.kotlinx.jupyter.api.DisplayResult
import org.jetbrains.kotlinx.jupyter.widget.display.WidgetDisplayController
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetFactoryRegistry
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel

/**
 * Manages the lifecycle and communication of Jupyter widgets.
 * Handles widget registration, state synchronization, and custom message passing.
 */
public interface WidgetManager {
    /**
     * Registry of available widget factories.
     */
    public val factoryRegistry: WidgetFactoryRegistry

    /**
     * Controller for managing Jupyter display logic.
     */
    public val displayController: WidgetDisplayController

    /**
     * If true, property updates received from the frontend are echoed back to the frontend.
     * This is sometimes necessary to ensure the frontend state remains in sync with the backend,
     * especially when the backend might override the requested change.
     *
     * In Jupyter Widgets 8, this is used for multiple frontend synchronization.
     * In Kotlin Notebook, this is disabled by default to save bandwidth as it's not the primary use case.
     */
    public var echoUpdateEnabled: Boolean

    /**
     * Retrieves a [WidgetModel] by its unique identifier.
     */
    public fun getWidget(modelId: String): WidgetModel?

    /**
     * Returns the unique identifier for a given [WidgetModel], or null if it's not registered.
     */
    public fun getWidgetId(widget: WidgetModel): String?

    /**
     * Registers a newly created widget with this manager.
     * This usually triggers a 'comm_open' message to the frontend.
     */
    public fun registerWidget(widget: WidgetModel)

    /**
     * Closes the widget, both locally and on the frontend.
     * This triggers a 'comm_close' message.
     */
    public fun closeWidget(widget: WidgetModel)

    /**
     * Returns a [DisplayResult] that allows displaying the widget in the notebook.
     */
    public fun renderWidget(widget: WidgetModel): DisplayResult

    /**
     * Sends a custom message to the frontend counterpart of the given widget.
     *
     * Custom messages are typically used for widget-specific actions that don't fit
     * the standard property-sync model (e.g., clearing the output in OutputWidget).
     */
    public fun sendCustomMessage(
        widget: WidgetModel,
        content: JsonObject,
        metadata: JsonElement? = null,
        buffers: List<ByteArray> = emptyList(),
    )
}
