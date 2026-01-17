package org.jetbrains.kotlinx.jupyter.widget.model.types.ranges

public object FloatRangeType : AbstractRangeType<Double, ClosedRange<Double>>("floatRange", 0.0..0.0) {
    override fun fromNumber(n: Number): Double = n.toDouble()

    override fun createRange(
        start: Double,
        end: Double,
    ): ClosedRange<Double> = start..end
}
