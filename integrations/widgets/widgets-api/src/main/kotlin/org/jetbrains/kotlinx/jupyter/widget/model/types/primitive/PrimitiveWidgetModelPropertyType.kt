package org.jetbrains.kotlinx.jupyter.widget.model.types.primitive

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue
import org.jetbrains.kotlinx.jupyter.widget.protocol.toPropertyValue
import org.jetbrains.kotlinx.jupyter.widget.protocol.toRawValue

/**
 * Base class for simple primitive property types.
 */
public abstract class PrimitiveWidgetModelPropertyType<T>(
    name: String,
    override val default: T,
) : AbstractWidgetModelPropertyType<T>(name) {
    override fun serialize(
        propertyValue: T,
        widgetManager: WidgetManager,
    ): RawPropertyValue = propertyValue.toPropertyValue()

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): T = patchValue.toRawValue() as T
}
