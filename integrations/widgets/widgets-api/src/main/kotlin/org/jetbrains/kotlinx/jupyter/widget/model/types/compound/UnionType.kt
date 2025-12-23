package org.jetbrains.kotlinx.jupyter.widget.model.types.compound

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.model.types.WidgetModelPropertyType

public class UnionType<T>(
    name: String,
    override val default: T,
    private val serializerSelector: (T) -> WidgetModelPropertyType<out T>,
    private val deserializers: List<WidgetModelPropertyType<out T>>,
) : AbstractWidgetModelPropertyType<T>(name) {
    override fun serialize(
        propertyValue: T,
        widgetManager: WidgetManager,
    ): Any? {
        val type = serializerSelector(propertyValue)
        @Suppress("UNCHECKED_CAST")
        return (type as WidgetModelPropertyType<T>).serialize(propertyValue, widgetManager)
    }

    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager,
    ): T {
        for (candidate in deserializers) {
            runCatching {
                @Suppress("UNCHECKED_CAST")
                return (candidate as WidgetModelPropertyType<T>).deserialize(patchValue, widgetManager)
            }
        }
        error("Unsupported value for $name: $patchValue")
    }
}
