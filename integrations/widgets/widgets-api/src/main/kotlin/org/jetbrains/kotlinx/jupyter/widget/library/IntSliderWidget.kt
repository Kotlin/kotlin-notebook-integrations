package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.globalWidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetFactory
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetSpec
import org.jetbrains.kotlinx.jupyter.widget.model.controlsSpec
import org.jetbrains.kotlinx.jupyter.widget.model.createAndRegisterWidget

public fun WidgetManager.intSlider(): IntSliderWidget = createAndRegisterWidget(IntSliderWidget.Factory::class)

public fun intSliderWidget(): IntSliderWidget = globalWidgetManager.intSlider()

public class IntSliderWidget internal constructor(
    spec: WidgetSpec,
) : DefaultWidgetModel(spec) {
    internal class Factory :
        DefaultWidgetFactory<IntSliderWidget>(
            controlsSpec("IntSlider"),
            ::IntSliderWidget,
        )

    public var value: Int by intProp("value", 0)
    public var min: Int by intProp("min", 0)
    public var max: Int by intProp("max", 100)
    public var step: Int by intProp("step", 1)
    public var description: String by stringProp("description", "")
    public var layout: LayoutWidget? by widgetProp("layout")
}
