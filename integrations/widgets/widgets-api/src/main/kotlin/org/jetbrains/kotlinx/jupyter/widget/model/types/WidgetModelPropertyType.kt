package org.jetbrains.kotlinx.jupyter.widget.model.types

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager

public interface WidgetModelPropertyType<T> {
    public val name: String
    public val default: T

    public fun serialize(propertyValue: T): Any?

    public fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager?,
    ): T
}
