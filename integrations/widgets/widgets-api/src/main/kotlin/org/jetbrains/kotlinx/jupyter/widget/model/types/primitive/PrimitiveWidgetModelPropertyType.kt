package org.jetbrains.kotlinx.jupyter.widget.model.types.primitive

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType

public abstract class PrimitiveWidgetModelPropertyType<T>(
    name: String,
    override val default: T,
) : AbstractWidgetModelPropertyType<T>(name) {
    override fun serialize(
        propertyValue: T,
        widgetManager: WidgetManager,
    ): Any? = propertyValue

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager,
    ): T = patchValue as T
}
