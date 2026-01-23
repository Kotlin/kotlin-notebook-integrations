package org.jetbrains.kotlinx.jupyter.widget.library.options

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetSpec
import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.NullableType
import org.jetbrains.kotlinx.jupyter.widget.model.types.ranges.IntRangeType

/**
 * Base class for range-based selection widgets.
 */
public abstract class SelectionRangeWidgetBase(
    spec: WidgetSpec,
    widgetManager: WidgetManager,
    fromFrontend: Boolean,
) : OptionWidgetBase<Any?, Pair<Any?, Any?>?, IntRange?>(spec, widgetManager, fromFrontend) {
    /**
     * Selected range of indices.
     * Maps to `index` on the frontend.
     */
    public override var index: IntRange? by prop("index", NullableType(IntRangeType), 0..0)

    /**
     * The labels of the selected range.
     */
    public var labels: Pair<String, String>?
        get() =
            index?.let { range ->
                val startLabel = optionsLabels.getOrNull(range.first) ?: ""
                val endLabel = optionsLabels.getOrNull(range.last) ?: ""
                startLabel to endLabel
            }
        set(v) {
            if (v == null) {
                index = null
                return
            }
            val start = optionsLabels.indexOf(v.first)
            val end = optionsLabels.indexOf(v.second)
            index =
                if (start != -1 && end != -1) {
                    start..end
                } else {
                    null
                }
        }

    public override var value: Pair<Any?, Any?>?
        get() =
            index?.let { range ->
                val start = optionsValues?.getOrNull(range.first) ?: optionsLabels.getOrNull(range.first)
                val end = optionsValues?.getOrNull(range.last) ?: optionsLabels.getOrNull(range.last)
                start to end
            }
        set(v) {
            index = findIndexByValue(v)
        }

    override fun findIndexByValue(v: Pair<Any?, Any?>?): IntRange? {
        if (v == null) return null
        val searchList = optionsValues ?: optionsLabels
        val start = searchList.indexOf(v.first)
        val end = searchList.indexOf(v.second)
        return if (start != -1 && end != -1) start..end else null
    }

    override fun labelToValue(label: String): Any? = label
}
