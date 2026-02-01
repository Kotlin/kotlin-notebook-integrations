package org.jetbrains.kotlinx.jupyter.widget.library

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetFactory
import org.jetbrains.kotlinx.jupyter.widget.model.createAndRegisterWidget

public fun WidgetManager.button(setup: ButtonWidget.() -> Unit = {}): ButtonWidget =
    createAndRegisterWidget(ButtonWidget.Factory).apply(setup)

/**
 * A button widget.
 */
public class ButtonWidget internal constructor(
    widgetManager: WidgetManager,
    fromFrontend: Boolean,
) : ButtonWidgetBase(widgetManager, fromFrontend) {
    /**
     * Factory for creating [ButtonWidget] instances.
     */
    internal object Factory : DefaultWidgetFactory<ButtonWidget>(buttonSpec, ::ButtonWidget)

    private val clickHandlers = mutableListOf<() -> Unit>()

    init {
        addCustomMessageListener { content, _, _ ->
            when (content.eventName) {
                "click" -> handleEvent(clickHandlers)
            }
        }
    }

    /**
     * Registers a callback to be called when the button is clicked.
     */
    public fun onClick(handler: () -> Unit) {
        clickHandlers.add(handler)
    }

    private val JsonObject.eventName: String? get() = this["event"]?.jsonPrimitive?.contentOrNull

    private fun handleEvent(eventHandlers: List<() -> Unit>) {
        for (handler in eventHandlers) {
            handler()
        }
    }
}
