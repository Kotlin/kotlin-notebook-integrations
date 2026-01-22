package org.jetbrains.kotlinx.jupyter.widget.model.types.primitive

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue

public object FloatType : PrimitiveWidgetModelPropertyType<Double>("float", 0.0) {
    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): Double {
        require(patchValue is RawPropertyValue.NumberValue) {
            "Expected WidgetValue.NumberValue for float, got ${patchValue::class.simpleName}"
        }
        return patchValue.value.toDouble()
    }
}
