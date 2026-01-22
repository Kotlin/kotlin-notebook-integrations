package org.jetbrains.kotlinx.jupyter.widget.model.types.compound

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.model.types.WidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue

/**
 * Property type representing a [List].
 * Serialized as a JSON array in the Jupyter protocol.
 */
public class ArrayType<E>(
    /**
     * Type information for the elements of the list.
     */
    public val elementType: WidgetModelPropertyType<E>,
) : AbstractWidgetModelPropertyType<List<E>>("array<${elementType.name}>") {
    override val default: List<E> = emptyList()

    override fun serialize(
        propertyValue: List<E>,
        widgetManager: WidgetManager,
    ): RawPropertyValue =
        RawPropertyValue.ListValue(
            propertyValue.map {
                elementType.serialize(it, widgetManager)
            },
        )

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): List<E> {
        require(patchValue is RawPropertyValue.ListValue) {
            "Expected WidgetValue.ListValue for $name, got ${patchValue::class.simpleName}"
        }
        return patchValue.values.map { valItem ->
            elementType.deserialize(valItem, widgetManager)
        }
    }
}
