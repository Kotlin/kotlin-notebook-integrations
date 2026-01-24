package org.jetbrains.kotlinx.jupyter.widget.model.types.primitive

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue
import org.jetbrains.kotlinx.jupyter.widget.protocol.toPropertyValue

public object StringType : PrimitiveWidgetModelPropertyType<String>("string", "") {
    override fun serialize(
        propertyValue: String,
        widgetManager: WidgetManager,
    ): RawPropertyValue = propertyValue.toPropertyValue()

    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): String = (patchValue as RawPropertyValue.StringValue).value
}
