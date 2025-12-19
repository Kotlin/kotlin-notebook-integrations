package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.globalWidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetFactory
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.controlsSpec
import org.jetbrains.kotlinx.jupyter.widget.model.createAndRegisterWidget

public fun WidgetManager.html(): HtmlWidget = createAndRegisterWidget(HtmlWidget.Factory)

public fun htmlWidget(): HtmlWidget = globalWidgetManager.html()

private val spec = controlsSpec("HTML")

public class HtmlWidget internal constructor(
    widgetManager: WidgetManager,
) : DefaultWidgetModel(spec, widgetManager) {
    internal object Factory : DefaultWidgetFactory<HtmlWidget>(spec, ::HtmlWidget)

    public var value: String by stringProp("value")
    public var layout: LayoutWidget? by widgetProp("layout")
}
