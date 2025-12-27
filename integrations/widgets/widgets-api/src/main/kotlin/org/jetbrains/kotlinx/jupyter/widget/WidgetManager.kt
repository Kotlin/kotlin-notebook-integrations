package org.jetbrains.kotlinx.jupyter.widget

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
}
