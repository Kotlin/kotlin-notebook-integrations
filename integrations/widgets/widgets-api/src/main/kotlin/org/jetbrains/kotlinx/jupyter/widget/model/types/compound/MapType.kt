package org.jetbrains.kotlinx.jupyter.widget.model.types.compound

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.model.types.WidgetModelPropertyType

public class MapType<V>(
    public val valueType: WidgetModelPropertyType<V>,
) : AbstractWidgetModelPropertyType<Map<String, V>>("map<${valueType.name}>") {
    override val default: Map<String, V> = emptyMap()

    override fun serialize(
        propertyValue: Map<String, V>,
        widgetManager: WidgetManager,
    ): Map<String, Any?> =
        propertyValue.mapValues { (_, value) ->
            valueType.serialize(value, widgetManager)
        }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager,
    ): Map<String, V> {
        require(patchValue is Map<*, *>) {
            "Expected Map for $name, got ${patchValue?.let { it::class.simpleName } ?: "null"}"
        }
        return patchValue.mapValues { (_, raw) ->
            valueType.deserialize(raw, widgetManager)
        } as Map<String, V>
    }
}
