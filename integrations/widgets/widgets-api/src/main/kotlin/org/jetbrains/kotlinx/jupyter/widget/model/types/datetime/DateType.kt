package org.jetbrains.kotlinx.jupyter.widget.model.types.datetime

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

/**
 * Property type for [LocalDate].
 * Serialized as an ISO-8601 string, e.g., "2023-01-01".
 */
public object DateType : AbstractWidgetModelPropertyType<LocalDate>("date") {
    override val default: LocalDate = LocalDate.EPOCH

    private val formatter: DateTimeFormatter =
        DateTimeFormatter
            .ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT)

    override fun serialize(
        propertyValue: LocalDate,
        widgetManager: WidgetManager,
    ): RawPropertyValue = RawPropertyValue.StringValue(propertyValue.format(formatter))

    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): LocalDate {
        require(patchValue is RawPropertyValue.StringValue) {
            "Expected WidgetValue.StringValue for date, got ${patchValue::class.simpleName}"
        }
        val value = patchValue.value
        return try {
            LocalDate.parse(value, formatter)
        } catch (e: Exception) {
            error("Invalid date format '$value', expected uuuu-MM-dd: ${e.message}")
        }
    }
}
