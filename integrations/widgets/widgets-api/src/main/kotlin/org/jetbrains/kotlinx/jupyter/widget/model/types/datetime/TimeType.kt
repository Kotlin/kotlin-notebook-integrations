package org.jetbrains.kotlinx.jupyter.widget.model.types.datetime

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

/**
 * Property type for [LocalTime].
 * Serialized as an ISO-8601 string, e.g., "12:00:00".
 */
public object TimeType : AbstractWidgetModelPropertyType<LocalTime>("time") {
    override val default: LocalTime = LocalTime.MIDNIGHT

    private val formatter: DateTimeFormatter =
        DateTimeFormatter
            .ofPattern("HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT)

    override fun serialize(
        propertyValue: LocalTime,
        widgetManager: WidgetManager,
    ): RawPropertyValue = RawPropertyValue.StringValue(propertyValue.format(formatter))

    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): LocalTime {
        require(patchValue is RawPropertyValue.StringValue) {
            "Expected WidgetValue.StringValue for time, got ${patchValue::class.simpleName}"
        }
        val value = patchValue.value
        return try {
            LocalTime.parse(value, formatter)
        } catch (e: Exception) {
            error("Invalid time format '$value', expected HH:mm:ss: ${e.message}")
        }
    }
}
