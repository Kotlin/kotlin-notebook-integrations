package org.jetbrains.kotlinx.jupyter.widget.model.types.compound

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue
import org.jetbrains.kotlinx.jupyter.widget.protocol.toPropertyValue
import org.jetbrains.kotlinx.jupyter.widget.protocol.toRawValue

/**
 * Property type for generic JSON-like objects (represented as a [Map] in Kotlin).
 */
public object RawObjectType : AbstractWidgetModelPropertyType<Map<String, Any?>>("object") {
    override val default: Map<String, Any?> = emptyMap()

    override fun serialize(
        propertyValue: Map<String, Any?>,
        widgetManager: WidgetManager,
    ): RawPropertyValue = propertyValue.toPropertyValue()

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): Map<String, Any?> {
        val raw = patchValue.toRawValue() ?: return emptyMap()
        require(raw is Map<*, *>) { "Expected Map for object, got ${raw::class.simpleName}" }
        return raw as Map<String, Any?>
    }
}
