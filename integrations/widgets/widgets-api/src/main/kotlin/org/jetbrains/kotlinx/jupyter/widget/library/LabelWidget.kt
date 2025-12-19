package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.globalWidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetFactory
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.controlsSpec
import org.jetbrains.kotlinx.jupyter.widget.model.createAndRegisterWidget

public fun WidgetManager.label(): LabelWidget = createAndRegisterWidget(LabelWidget.Factory)

public fun labelWidget(): LabelWidget = globalWidgetManager.label()

private val spec = controlsSpec("Label")

public class LabelWidget internal constructor(
    widgetManager: WidgetManager,
) : DefaultWidgetModel(spec, widgetManager) {
    internal object Factory : DefaultWidgetFactory<LabelWidget>(spec, ::LabelWidget)

    public var value: String by stringProp("value", "")
}
