package org.jetbrains.kotlinx.jupyter.widget.model.types.ranges

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType

public abstract class AbstractRangeType<T : Comparable<T>, R : ClosedRange<T>>(
    name: String,
    override val default: R,
) : AbstractWidgetModelPropertyType<R>(name) {
    protected abstract fun fromNumber(n: Number): T

    protected abstract fun createRange(
        start: T,
        end: T,
    ): R

    override fun serialize(
        propertyValue: R,
        widgetManager: WidgetManager,
    ): List<Any?> = listOf(propertyValue.start, propertyValue.endInclusive)

    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager,
    ): R {
        require(patchValue is List<*> && patchValue.size == 2) {
            "Expected List of size 2 for $name, got $patchValue"
        }
        return createRange(
            fromNumber(patchValue[0] as Number),
            fromNumber(patchValue[1] as Number),
        )
    }
}
