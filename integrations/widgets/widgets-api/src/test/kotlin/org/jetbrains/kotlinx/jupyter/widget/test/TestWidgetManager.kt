package org.jetbrains.kotlinx.jupyter.widget.test

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.jetbrains.kotlinx.jupyter.api.DisplayResult
import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.display.WidgetDisplayController
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetFactoryRegistry
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel

interface TestWidgetManager : WidgetManager {
    override val factoryRegistry: WidgetFactoryRegistry get() = notImplemented()
    override val displayController: WidgetDisplayController get() = notImplemented()
    override var echoUpdateEnabled: Boolean
        get() = false
        set(_) {}

    override fun getWidget(modelId: String): WidgetModel? = notImplemented()

    override fun getWidgetId(widget: WidgetModel): String? = notImplemented()

    override fun registerWidget(widget: WidgetModel) {}

    override fun closeWidget(widget: WidgetModel) {}

    override fun renderWidget(widget: WidgetModel): DisplayResult = notImplemented()

    override fun sendCustomMessage(
        widget: WidgetModel,
        content: JsonObject,
        metadata: JsonElement?,
        buffers: List<ByteArray>,
    ) = notImplemented()

    companion object {
        fun notImplemented(): Nothing = throw NotImplementedError("Not implemented")

        val INSTANCE = object : TestWidgetManager {}
    }
}
