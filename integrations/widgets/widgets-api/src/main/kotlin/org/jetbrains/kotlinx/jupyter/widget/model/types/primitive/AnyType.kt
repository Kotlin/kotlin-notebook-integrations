package org.jetbrains.kotlinx.jupyter.widget.model.types.primitive

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue
import org.jetbrains.kotlinx.jupyter.widget.protocol.toPropertyValue
import org.jetbrains.kotlinx.jupyter.widget.protocol.toRawValue

public object AnyType : AbstractWidgetModelPropertyType<Any?>("any") {
    override val default: Any? = null

    override fun serialize(
        propertyValue: Any?,
        widgetManager: WidgetManager,
    ): RawPropertyValue = propertyValue.toPropertyValue()

    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): Any? = patchValue.toRawValue()
}
