package org.jetbrains.kotlinx.jupyter.widget.library.options

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetSpec
import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.ArrayType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.IntType

/**
 * Base class for selection widgets with multiple selection.
 */
public abstract class MultipleSelectionWidgetBase(
    spec: WidgetSpec,
    widgetManager: WidgetManager,
    fromFrontend: Boolean,
) : OptionWidgetBase<Any?, List<Any?>, List<Int>>(spec, widgetManager, fromFrontend) {
    /**
     * Selected indices.
     * Maps to `index` on the frontend.
     */
    public override var index: List<Int> by prop("index", ArrayType(IntType), emptyList())

    /**
     * The labels of the selected options.
     */
    public var labels: List<String>
        get() = index.mapNotNull { i -> optionsLabels.getOrNull(i) }
        set(v) {
            index = v.map { label -> optionsLabels.indexOf(label) }
        }

    public override var value: List<Any?>
        get() =
            index.map { i ->
                optionsValues?.getOrNull(i) ?: optionsLabels.getOrNull(i)
            }
        set(v) {
            index = findIndexByValue(v)
        }

    override fun findIndexByValue(v: List<Any?>): List<Int> {
        val searchList = optionsValues ?: optionsLabels
        return v.mapNotNull { item ->
            searchList.indexOf(item).takeIf { it != -1 }
        }
    }

    override fun labelToValue(label: String): Any? = label
}
