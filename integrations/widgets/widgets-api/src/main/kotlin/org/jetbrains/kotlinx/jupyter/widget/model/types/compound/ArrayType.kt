package org.jetbrains.kotlinx.jupyter.widget.model.types.compound

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.model.types.WidgetModelPropertyType

public class ArrayType<E>(
    public val elementType: WidgetModelPropertyType<E>,
) : AbstractWidgetModelPropertyType<List<E>>("array<${elementType.name}>") {
    override val default: List<E> = emptyList()

    override fun serialize(propertyValue: List<E>): List<Any?> = propertyValue.map { elementType.serialize(it) }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager?,
    ): List<E> {
        require(patchValue is List<*>) {
            "Expected List for $name, got ${patchValue?.let { it::class.simpleName } ?: "null"}"
        }
        return patchValue.map { raw ->
            elementType.deserialize(raw, widgetManager)
        }
    }
}
