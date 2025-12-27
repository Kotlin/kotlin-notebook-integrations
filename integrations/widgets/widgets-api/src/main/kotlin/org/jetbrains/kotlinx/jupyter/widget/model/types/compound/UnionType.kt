package org.jetbrains.kotlinx.jupyter.widget.model.types.compound

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType

public class UnionType<T>(
    name: String,
    override val default: T,
    private val serializer: (T, WidgetManager) -> Any?,
    private val deserializers: List<(Any?, WidgetManager) -> T>,
) : AbstractWidgetModelPropertyType<T>(name) {
    override fun serialize(
        propertyValue: T,
        widgetManager: WidgetManager,
    ): Any? = serializer(propertyValue, widgetManager)

    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager,
    ): T {
        val errors = mutableListOf<Throwable>()
        for (deserializer in deserializers) {
            try {
                return deserializer(patchValue, widgetManager)
            } catch (e: Exception) {
                errors.add(e)
            }
        }
        error(
            "Unsupported value for property $name: '$patchValue'. " +
                "Tried ${deserializers.size} deserializers. " +
                "Errors: ${errors.joinToString { it.message ?: it.toString() }}",
        )
    }
}
