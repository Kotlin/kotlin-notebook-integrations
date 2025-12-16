package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.globalWidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetFactory
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetSpec
import org.jetbrains.kotlinx.jupyter.widget.model.controlsSpec
import org.jetbrains.kotlinx.jupyter.widget.model.createAndRegisterWidget

public fun WidgetManager.output(): OutputWidget = createAndRegisterWidget(OutputWidget.Factory::class)

public fun outputWidget(): OutputWidget = globalWidgetManager.output()

public class OutputWidget internal constructor(
    spec: WidgetSpec,
) : DefaultWidgetModel(spec) {
    internal class Factory :
        DefaultWidgetFactory<OutputWidget>(
            controlsSpec("Output"),
            ::OutputWidget,
        )

    public var layout: LayoutWidget? by widgetProp("layout")
}
