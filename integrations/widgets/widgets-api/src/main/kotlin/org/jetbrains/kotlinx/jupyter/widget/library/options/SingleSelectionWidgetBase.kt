package org.jetbrains.kotlinx.jupyter.widget.library.options

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetSpec

/**
 * Base class for selection widgets with non-nullable single selection.
 */
public abstract class SingleSelectionWidgetBase(
    spec: WidgetSpec,
    widgetManager: WidgetManager,
    fromFrontend: Boolean,
) : OptionWidgetBase<Any?, Any?, Int>(spec, widgetManager, fromFrontend) {
    /**
     * Selected index.
     * Maps to `index` on the frontend.
     */
    public override var index: Int by intProp("index", 0)

    /**
     * The label of the selected option.
     */
    public var label: String
        get() = optionsLabels.getOrNull(index) ?: ""
        set(v) {
            val i = optionsLabels.indexOf(v)
            if (i != -1) index = i
        }

    public override var value: Any?
        get() {
            return optionsValues?.getOrNull(index) ?: optionsLabels.getOrNull(index)
        }
        set(v) {
            index = findIndexByValue(v)
        }

    override fun findIndexByValue(v: Any?): Int {
        val searchList = optionsValues ?: optionsLabels
        val i = searchList.indexOf(v)
        return if (i != -1) i else index
    }

    override fun labelToValue(label: String): Any = label
}
