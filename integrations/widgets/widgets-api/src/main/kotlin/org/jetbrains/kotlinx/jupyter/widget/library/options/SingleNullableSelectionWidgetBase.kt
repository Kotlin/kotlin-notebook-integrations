package org.jetbrains.kotlinx.jupyter.widget.library.options

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetSpec

/**
 * Base class for selection widgets with possibly nullable single selection.
 */
public abstract class SingleNullableSelectionWidgetBase(
    spec: WidgetSpec,
    widgetManager: WidgetManager,
    fromFrontend: Boolean,
) : OptionWidgetBase<Any?, Any?, Int?>(spec, widgetManager, fromFrontend) {
    /**
     * Selected index.
     * Maps to `index` on the frontend.
     */
    public override var index: Int? by nullableIntProp("index", null)

    /**
     * The label of the selected option.
     */
    public var label: String?
        get() = index?.let { i -> optionsLabels.getOrNull(i) }
        set(v) {
            index = if (v == null) null else optionsLabels.indexOf(v).takeIf { it != -1 }
        }

    public override var value: Any?
        get() =
            index?.let { i ->
                optionsValues?.getOrNull(i) ?: optionsLabels.getOrNull(i)
            }
        set(v) {
            index = findIndexByValue(v)
        }

    override fun findIndexByValue(v: Any?): Int? {
        if (v == null) return null
        val searchList = optionsValues ?: optionsLabels
        val i = searchList.indexOf(v)
        return if (i != -1) i else null
    }

    override fun labelToValue(label: String): Any? = label
}
