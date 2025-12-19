package org.jetbrains.kotlinx.jupyter.widget.model.types.datetime

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

public object DateType : AbstractWidgetModelPropertyType<LocalDate>("date") {
    override val default: LocalDate = LocalDate.EPOCH

    private val formatter: DateTimeFormatter =
        DateTimeFormatter
            .ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT)

    override fun serialize(
        propertyValue: LocalDate,
        widgetManager: WidgetManager,
    ): Any? = propertyValue.format(formatter)

    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager,
    ): LocalDate {
        require(patchValue is String) {
            "Expected String for date, got ${patchValue?.let { it::class.simpleName } ?: "null"}"
        }
        return try {
            LocalDate.parse(patchValue, formatter)
        } catch (e: Exception) {
            error("Invalid date format '$patchValue', expected uuuu-MM-dd: ${e.message}")
        }
    }
}
