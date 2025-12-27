package org.jetbrains.kotlinx.jupyter.widget

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.jetbrains.kotlinx.jupyter.api.DisplayResult
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetFactoryRegistry
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel

public interface WidgetManager {
    public val factoryRegistry: WidgetFactoryRegistry

    public var echoUpdateEnabled: Boolean

    public fun getWidget(modelId: String): WidgetModel?

    public fun getWidgetId(widget: WidgetModel): String?

    public fun registerWidget(widget: WidgetModel)

    public fun renderWidget(widget: WidgetModel): DisplayResult

    public fun sendCustomMessage(
        widget: WidgetModel,
        content: JsonObject,
        metadata: JsonElement? = null,
        buffers: List<ByteArray> = emptyList(),
    )
}
