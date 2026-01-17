package org.jetbrains.kotlinx.jupyter.widget.library

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.jetbrains.kotlinx.jupyter.protocol.api.id
import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetFactory
import org.jetbrains.kotlinx.jupyter.widget.model.createAndRegisterWidget

public fun WidgetManager.output(setup: OutputWidget.() -> Unit = {}): OutputWidget =
    createAndRegisterWidget(OutputWidget.Factory).apply(setup)

public class OutputWidget internal constructor(
    widgetManager: WidgetManager,
    fromFrontend: Boolean,
) : OutputWidgetBase(widgetManager, fromFrontend) {
    private var scopeCounter = 0

    internal object Factory : DefaultWidgetFactory<OutputWidget>(outputSpec, ::OutputWidget)

    public fun clearOutput(wait: Boolean = false) {
        sendCustomMessage(
            buildJsonObject {
                put("method", JsonPrimitive("clear_output"))
                put("wait", JsonPrimitive(wait))
            },
        )
    }

    public fun withScope(action: () -> Unit) {
        scopeCounter++
        val parentId = widgetManager.contextMessage?.id
        if (parentId != null) msgId = parentId
        try {
            action()
        } finally {
            scopeCounter--
            if (scopeCounter == 0) {
                msgId = ""
            }
        }
    }
}
