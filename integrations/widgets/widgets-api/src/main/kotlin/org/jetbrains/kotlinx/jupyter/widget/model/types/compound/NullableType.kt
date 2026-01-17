package org.jetbrains.kotlinx.jupyter.widget.model.types.compound

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.model.types.WidgetModelPropertyType

/**
 * Property type for nullable values.
 * Wraps an [inner] type and handles `null` values by serializing them as `null` in JSON.
 */
public class NullableType<T>(
    private val inner: WidgetModelPropertyType<T>,
) : AbstractWidgetModelPropertyType<T?>("${inner.name}?") {
    override val default: T? = null

    override fun serialize(
        propertyValue: T?,
        widgetManager: WidgetManager,
    ): Any? =
        propertyValue?.let {
            inner.serialize(it, widgetManager)
        }

    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager,
    ): T? = if (patchValue == null) null else inner.deserialize(patchValue, widgetManager)
}
