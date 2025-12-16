package org.jetbrains.kotlinx.jupyter.widget.model.types.compound

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.model.types.WidgetModelPropertyType

public class NullableType<T>(
    private val inner: WidgetModelPropertyType<T>,
) : AbstractWidgetModelPropertyType<T?>("${inner.name}?") {
    override val default: T? = null

    override fun serialize(propertyValue: T?): Any? = propertyValue?.let { inner.serialize(it) }

    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager?,
    ): T? = if (patchValue == null) null else inner.deserialize(patchValue, widgetManager)
}
