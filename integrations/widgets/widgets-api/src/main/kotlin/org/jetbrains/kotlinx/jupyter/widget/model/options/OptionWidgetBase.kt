package org.jetbrains.kotlinx.jupyter.widget.model.options

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetSpec
import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.ArrayType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.StringType

/**
 * Base class for option-based widgets.
 */
public abstract class OptionWidgetBase<ValueT, ValueSelectionT, IndexSelectionT>(
    spec: WidgetSpec,
    widgetManager: WidgetManager,
) : DefaultWidgetModel(spec, widgetManager) {
    /**
     * The labels for the options.
     * Maps to `_options_labels` on the frontend.
     */
    public var optionsLabels: List<String> by prop(
        "_options_labels",
        ArrayType(
            StringType,
        ),
        emptyList(),
    )

    protected var optionsValues: List<ValueT>? = null

    /**
     * Selected index(es).
     */
    public abstract var index: IndexSelectionT

    /**
     * The options to choose from.
     * List of pairs from labels to values.
     */
    public var options: List<Pair<String, ValueT>>
        get() = optionsLabels.zip(optionsValues ?: optionsLabels.map(::labelToValue))
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
            optionsValues = value.map(::labelToValue)
            index = findIndexByValue(currentValue)
        }

    /**
     * The value of the selected option.
     */
    public abstract var value: ValueSelectionT

    protected abstract fun findIndexByValue(v: ValueSelectionT): IndexSelectionT

    protected abstract fun labelToValue(label: String): ValueT
}
