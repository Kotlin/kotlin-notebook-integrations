package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.globalWidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetFactory
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.createAndRegisterWidget
import org.jetbrains.kotlinx.jupyter.widget.model.outputSpec

public fun WidgetManager.output(): OutputWidget = createAndRegisterWidget(OutputWidget.Factory)

public fun outputWidget(): OutputWidget = globalWidgetManager.output()

private val spec = outputSpec("Output")

public class OutputWidget internal constructor(
    widgetManager: WidgetManager,
) : DefaultWidgetModel(spec, widgetManager) {
    internal object Factory : DefaultWidgetFactory<OutputWidget>(spec, ::OutputWidget)

    public var layout: LayoutWidget? by widgetProp("layout", widgetManager.layout())
}
