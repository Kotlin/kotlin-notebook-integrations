package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.globalWidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetFactory
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.baseSpec
import org.jetbrains.kotlinx.jupyter.widget.model.createAndRegisterWidget

public fun WidgetManager.layout(): LayoutWidget = createAndRegisterWidget(LayoutWidget.Factory)

public fun layoutWidget(): LayoutWidget = globalWidgetManager.layout()

private val spec = baseSpec("Layout")

public class LayoutWidget private constructor() : DefaultWidgetModel(spec) {
    internal object Factory : DefaultWidgetFactory<LayoutWidget>(spec, ::LayoutWidget)

    public var layout: String by stringProp("layout", "")

    public var width: String by stringProp("width", "")
    public var height: String by stringProp("height", "")
    public var display: String by stringProp("display", "")
    public var margin: String by stringProp("margin", "")
    public var padding: String by stringProp("padding", "")
}
