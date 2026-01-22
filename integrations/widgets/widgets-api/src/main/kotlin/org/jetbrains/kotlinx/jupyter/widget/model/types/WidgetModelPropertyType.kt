package org.jetbrains.kotlinx.jupyter.widget.model.types

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue

/**
 * Defines how a widget property of type [T] is serialized and deserialized
 * when communicating with the Jupyter frontend.
 */
public interface WidgetModelPropertyType<T> {
    /**
     * Human-readable name of the type.
     */
    public val name: String

    /**
     * Default value for this property type.
     */
    public val default: T

    /**
     * Converts a Kotlin property value to a JSON-compatible representation.
     * Some types (like widget references) may need the [widgetManager] for lookups.
     */
    public fun serialize(
        propertyValue: T,
        widgetManager: WidgetManager,
    ): RawPropertyValue

    /**
     * Converts a JSON-compatible representation back to a Kotlin property value.
     */
    public fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): T
}
