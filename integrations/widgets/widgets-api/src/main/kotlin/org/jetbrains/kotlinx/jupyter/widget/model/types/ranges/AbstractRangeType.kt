package org.jetbrains.kotlinx.jupyter.widget.model.types.ranges

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue
import org.jetbrains.kotlinx.jupyter.widget.protocol.toPropertyValue

/**
 * Base class for property types representing a range (e.g., [IntRange]).
 * In the Jupyter Widgets protocol, ranges are represented as a 2-element list [start, end].
 */
public abstract class AbstractRangeType<T, R : ClosedRange<T>>(
    name: String,
    override val default: R,
) : AbstractWidgetModelPropertyType<R>(name) where T : Comparable<T>, T : Number {
    /**
     * Converts a generic [Number] to the specific numeric type [T].
     */
    protected abstract fun fromNumber(n: Number): T

    /**
     * Creates a range object of type [R] from start and end values.
     */
    protected abstract fun createRange(
        start: T,
        end: T,
    ): R

    override fun serialize(
        propertyValue: R,
        widgetManager: WidgetManager,
    ): RawPropertyValue =
        RawPropertyValue.ListValue(
            listOf(
                propertyValue.start.toPropertyValue(),
                propertyValue.endInclusive.toPropertyValue(),
            ),
        )

    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): R {
        require(patchValue is RawPropertyValue.ListValue && patchValue.values.size == 2) {
            "Expected List of size 2 for $name, got $patchValue"
        }
        val startVal = patchValue.values[0]
        val endVal = patchValue.values[1]
        require(startVal is RawPropertyValue.NumberValue && endVal is RawPropertyValue.NumberValue) {
            "Expected Numbers in range list, got $startVal and $endVal"
        }

        return createRange(
            fromNumber(startVal.value),
            fromNumber(endVal.value),
        )
    }
}
