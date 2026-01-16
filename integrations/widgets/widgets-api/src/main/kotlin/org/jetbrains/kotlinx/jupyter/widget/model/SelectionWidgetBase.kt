package org.jetbrains.kotlinx.jupyter.widget.model

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.ArrayType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.StringType

/**
 * Base class for selection widgets.
 * Selection widgets have a list of options and a selected index.
 */
public abstract class SelectionWidgetBase(
    spec: WidgetSpec,
    widgetManager: WidgetManager,
) : DefaultWidgetModel(spec, widgetManager) {
    /**
     * The labels for the options.
     * Maps to `_options_labels` on the frontend.
     */
    internal var optionsLabels: List<String> by prop(
        "_options_labels",
        ArrayType(
            StringType,
        ),
        emptyList(),
    )

    private var optionsValues: List<Any?>? = null

    /**
     * Selected index.
     * Maps to `index` on the frontend.
     */
    public open var index: Int? by nullableIntProp("index", null)

    /**
     * The options to choose from.
     * List of pairs from labels to values.
     */
    public var options: List<Pair<String, Any?>>
        get() = optionsLabels.zip(optionsValues ?: optionsLabels)
        set(value) {
            val currentValue = this.value
            optionsLabels = value.map { it.first }
            optionsValues = value.map { it.second }
            index = findIndexByValue(currentValue)
        }

    /**
     * The options to choose from.
     * List of labels. Values are automatically generated from labels.
     */
    public var simpleOptions: List<String>
        get() = optionsLabels
        set(value) {
            val currentValue = this.value
            optionsLabels = value
            optionsValues = value
            index = findIndexByValue(currentValue)
        }

    /**
     * The label of the selected option.
     */
    public var label: String?
        get() = index?.let { i -> optionsLabels.getOrNull(i) }
        set(v) {
            index = if (v == null) null else optionsLabels.indexOf(v).takeIf { it != -1 }
        }

    /**
     * The value of the selected option.
     */
    public var value: Any?
        get() =
            index?.let { i ->
                optionsValues?.getOrNull(i) ?: optionsLabels.getOrNull(i)
            }
        set(v) {
            index = findIndexByValue(v)
        }

    private fun findIndexByValue(v: Any?): Int? {
        if (v == null) return null
        val searchList = optionsValues ?: optionsLabels
        val i = searchList.indexOf(v)
        return if (i != -1) i else null
    }
}
