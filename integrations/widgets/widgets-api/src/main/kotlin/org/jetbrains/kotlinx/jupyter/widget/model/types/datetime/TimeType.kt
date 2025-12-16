package org.jetbrains.kotlinx.jupyter.widget.model.types.datetime

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

public object TimeType : AbstractWidgetModelPropertyType<LocalTime>("time") {
    override val default: LocalTime = LocalTime.MIDNIGHT

    private val formatter: DateTimeFormatter =
        DateTimeFormatter
            .ofPattern("HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT)

    override fun serialize(propertyValue: LocalTime): Any? = propertyValue.format(formatter)

    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager?,
    ): LocalTime {
        require(patchValue is String) {
            "Expected String for time, got ${patchValue?.let { it::class.simpleName } ?: "null"}"
        }
        return try {
            LocalTime.parse(patchValue, formatter)
        } catch (e: Exception) {
            error("Invalid time format '$patchValue', expected HH:mm:ss: ${e.message}")
        }
    }
}
