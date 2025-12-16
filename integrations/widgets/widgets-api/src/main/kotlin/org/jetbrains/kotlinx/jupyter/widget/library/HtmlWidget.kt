package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.globalWidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetFactory
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetSpec
import org.jetbrains.kotlinx.jupyter.widget.model.controlsSpec
import org.jetbrains.kotlinx.jupyter.widget.model.createAndRegisterWidget

public fun WidgetManager.html(): HtmlWidget = createAndRegisterWidget(HtmlWidget.Factory::class)

public fun htmlWidget(): HtmlWidget = globalWidgetManager.html()

public class HtmlWidget internal constructor(
    spec: WidgetSpec,
) : DefaultWidgetModel(spec) {
    internal class Factory :
        DefaultWidgetFactory<HtmlWidget>(
            controlsSpec("HTML"),
            ::HtmlWidget,
        )

    public var value: String by stringProp("value")
    public var layout: LayoutWidget? by widgetProp("layout")
}
