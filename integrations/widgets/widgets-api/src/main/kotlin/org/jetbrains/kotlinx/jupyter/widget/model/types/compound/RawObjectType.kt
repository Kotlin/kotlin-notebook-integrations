package org.jetbrains.kotlinx.jupyter.widget.model.types.compound

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType

public object RawObjectType : AbstractWidgetModelPropertyType<Map<String, Any?>>("object") {
    override val default: Map<String, Any?> = emptyMap()

    override fun serialize(
        propertyValue: Map<String, Any?>,
        widgetManager: WidgetManager,
    ): Any = propertyValue

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager,
    ): Map<String, Any?> {
        if (patchValue == null) return emptyMap()
        require(patchValue is Map<*, *>) { "Expected Map for object, got ${patchValue::class.simpleName}" }
        return patchValue as Map<String, Any?>
    }
}
