package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.globalWidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetFactory
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetSpec
import org.jetbrains.kotlinx.jupyter.widget.model.controlsSpec
import org.jetbrains.kotlinx.jupyter.widget.model.createAndRegisterWidget

public fun WidgetManager.label(): LabelWidget = createAndRegisterWidget(LabelWidget.Factory::class)

public fun labelWidget(): LabelWidget = globalWidgetManager.label()

public class LabelWidget internal constructor(
    spec: WidgetSpec,
) : DefaultWidgetModel(spec) {
    internal class Factory :
        DefaultWidgetFactory<LabelWidget>(
            controlsSpec("Label"),
            ::LabelWidget,
        )

    public var value: String by stringProp("value", "")
}
