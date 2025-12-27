package org.jetbrains.kotlinx.jupyter.widget.model.types.primitive

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager

public object FloatType : PrimitiveWidgetModelPropertyType<Double>("float", 0.0) {
    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager,
    ): Double = (patchValue as Number).toDouble()
}
