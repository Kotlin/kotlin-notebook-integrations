package org.jetbrains.kotlinx.jupyter.widget.model.types.ranges

public object IntRangeType : AbstractRangeType<Int, IntRange>("intRange", 0..0) {
    override fun fromNumber(n: Number): Int = n.toInt()

    override fun createRange(
        start: Int,
        end: Int,
    ): IntRange = start..end
}
