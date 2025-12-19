package org.jetbrains.kotlinx.jupyter.widget.model.types.datetime

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

public object DatetimeType : AbstractWidgetModelPropertyType<Instant>("datetime") {
    override val default: Instant = Instant.EPOCH

    private val formatter: DateTimeFormatter =
        DateTimeFormatter
            .ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'")
            .withResolverStyle(ResolverStyle.STRICT)
            .withZone(ZoneOffset.UTC)

    override fun serialize(
        propertyValue: Instant,
        widgetManager: WidgetManager,
    ): Any? = formatter.format(propertyValue) // respect 'Z'

    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager,
    ): Instant {
        require(patchValue is String) {
            "Expected String for datetime, got ${patchValue?.let { it::class.simpleName } ?: "null"}"
        }
        return try {
            ZonedDateTime.parse(patchValue, formatter).toInstant()
        } catch (e: Exception) {
            error("Invalid datetime format '$patchValue', expected uuuu-MM-dd'T'HH:mm:ss'Z': ${e.message}")
        }
    }
}
