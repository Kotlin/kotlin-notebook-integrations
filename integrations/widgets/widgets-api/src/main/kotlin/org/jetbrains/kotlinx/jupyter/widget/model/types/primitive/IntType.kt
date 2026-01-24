package org.jetbrains.kotlinx.jupyter.widget.model.types.primitive

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue
import org.jetbrains.kotlinx.jupyter.widget.protocol.toPropertyValue

/**
 * Property type for [Int].
 */
public object IntType : PrimitiveWidgetModelPropertyType<Int>("int", 0) {
    override fun serialize(
        propertyValue: Int,
        widgetManager: WidgetManager,
    ): RawPropertyValue = propertyValue.toPropertyValue()

    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): Int {
        require(patchValue is RawPropertyValue.NumberValue) {
            "Expected WidgetValue.NumberValue for int, got ${patchValue::class.simpleName}"
        }
        return patchValue.value.toInt()
    }
}
