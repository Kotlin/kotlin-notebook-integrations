package org.jetbrains.kotlinx.jupyter.widget.model.types.datetime

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

/**
 * Property type for [Instant].
 * Serialized as an ISO-8601 string in UTC, e.g., "2023-01-01T12:00:00Z".
 */
public object DatetimeType : AbstractWidgetModelPropertyType<Instant>("datetime") {
    /**
     * Default value is [Instant.EPOCH].
     */
    override val default: Instant = Instant.EPOCH

    private val formatter: DateTimeFormatter =
        DateTimeFormatter
            .ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'")
            .withResolverStyle(ResolverStyle.STRICT)
            .withZone(ZoneOffset.UTC)

    override fun serialize(
        propertyValue: Instant,
        widgetManager: WidgetManager,
    ): RawPropertyValue = RawPropertyValue.StringValue(formatter.format(propertyValue)) // respect 'Z'

    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): Instant {
        require(patchValue is RawPropertyValue.StringValue) {
            "Expected WidgetValue.StringValue for datetime, got ${patchValue::class.simpleName}"
        }
        val value = patchValue.value
        return try {
            ZonedDateTime.parse(value, formatter).toInstant()
        } catch (e: Exception) {
            error("Invalid datetime format '$value', expected uuuu-MM-dd'T'HH:mm:ss'Z': ${e.message}")
        }
    }
}
