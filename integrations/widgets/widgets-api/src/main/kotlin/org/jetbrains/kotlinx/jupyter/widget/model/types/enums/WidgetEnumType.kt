package org.jetbrains.kotlinx.jupyter.widget.model.types.enums

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue

public class WidgetEnumType<E : WidgetEnum<E>>(
    private val widgetEnum: E,
    override val default: WidgetEnumEntry<E>,
) : AbstractWidgetModelPropertyType<WidgetEnumEntry<E>>("enum") {
    override fun serialize(
        propertyValue: WidgetEnumEntry<E>,
        widgetManager: WidgetManager,
    ): RawPropertyValue = RawPropertyValue.StringValue(propertyValue.name)

    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): WidgetEnumEntry<E> {
        require(patchValue is RawPropertyValue.StringValue) {
            "Expected WidgetValue.StringValue for enum, got ${patchValue::class.simpleName}"
        }
        val name = patchValue.value
        return widgetEnum.entries.first {
            it.name == name
        }
    }
}
