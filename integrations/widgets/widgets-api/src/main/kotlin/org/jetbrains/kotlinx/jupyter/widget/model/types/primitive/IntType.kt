package org.jetbrains.kotlinx.jupyter.widget.model.types.primitive

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager

/**
 * Property type for [Int].
 */
public object IntType : PrimitiveWidgetModelPropertyType<Int>("int", 0) {
    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager,
    ): Int = (patchValue as Number).toInt()
}
