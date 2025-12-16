package org.jetbrains.kotlinx.jupyter.widget.model.types.enums

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType

public class WidgetEnumType<E : WidgetEnum<E>>(
    private val widgetEnum: E,
    override val default: WidgetEnumEntry<E>,
) : AbstractWidgetModelPropertyType<WidgetEnumEntry<E>>("enum") {
    override fun serialize(propertyValue: WidgetEnumEntry<E>): String = propertyValue.name

    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager?,
    ): WidgetEnumEntry<E> {
        require(patchValue is String) {
            "Expected String for enum, got ${patchValue?.let { it::class.simpleName } ?: "null"}"
        }
        return widgetEnum.entries.first {
            it.name == patchValue
        }
    }
}
