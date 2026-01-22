package org.jetbrains.kotlinx.jupyter.widget.model.types.compound

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue

/**
 * Property type for values that can be of multiple different types (e.g., Int or String).
 *
 * It uses a list of [deserializers] and returns the result of the first one that completes without an exception.
 */
public class UnionType<T>(
    name: String,
    override val default: T,
    private val serializer: (T, WidgetManager) -> RawPropertyValue,
    private val deserializers: List<(RawPropertyValue, WidgetManager) -> T>,
) : AbstractWidgetModelPropertyType<T>(name) {
    /**
     * Serializes the value using the provided [serializer] function.
     */
    override fun serialize(
        propertyValue: T,
        widgetManager: WidgetManager,
    ): RawPropertyValue = serializer(propertyValue, widgetManager)

    /**
     * Attempts to deserialize the value by trying all provided [deserializers] in order.
     * Throws an [IllegalStateException] if no deserializer succeeds.
     */
    override fun deserialize(
        patchValue: RawPropertyValue,
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
