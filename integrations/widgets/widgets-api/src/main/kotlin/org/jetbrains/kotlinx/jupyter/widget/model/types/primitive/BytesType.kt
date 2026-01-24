package org.jetbrains.kotlinx.jupyter.widget.model.types.primitive

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue
import org.jetbrains.kotlinx.jupyter.widget.protocol.toPropertyValue

public object BytesType : PrimitiveWidgetModelPropertyType<ByteArray>("bytes", byteArrayOf()) {
    override fun serialize(
        propertyValue: ByteArray,
        widgetManager: WidgetManager,
    ): RawPropertyValue = propertyValue.toPropertyValue()

    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): ByteArray = (patchValue as RawPropertyValue.ByteArrayValue).value
}
