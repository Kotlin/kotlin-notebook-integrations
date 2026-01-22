package org.jetbrains.kotlinx.jupyter.widget.model.types.compound

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.model.types.WidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue

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
    ): RawPropertyValue =
        if (propertyValue == null) {
            RawPropertyValue.Null
        } else {
            inner.serialize(propertyValue, widgetManager)
        }

    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): T? = if (patchValue is RawPropertyValue.Null) null else inner.deserialize(patchValue, widgetManager)
}
