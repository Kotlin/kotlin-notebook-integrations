package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.globalWidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetFactory
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetSpec
import org.jetbrains.kotlinx.jupyter.widget.model.baseSpec
import org.jetbrains.kotlinx.jupyter.widget.model.createAndRegisterWidget

public fun WidgetManager.layout(): LayoutWidget = createAndRegisterWidget(LayoutWidget.Factory::class)

public fun layoutWidget(): LayoutWidget = globalWidgetManager.layout()

public class LayoutWidget private constructor(
    spec: WidgetSpec,
) : DefaultWidgetModel(spec) {
    internal class Factory :
        DefaultWidgetFactory<LayoutWidget>(
            baseSpec("Layout"),
            ::LayoutWidget,
        )

    public var layout: String by stringProp("layout", "")

    public var width: String by stringProp("width", "")
    public var height: String by stringProp("height", "")
    public var display: String by stringProp("display", "")
    public var margin: String by stringProp("margin", "")
    public var padding: String by stringProp("padding", "")
}
