package org.jetbrains.kotlinx.jupyter.widget.library.options

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetSpec
import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.ArrayType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.StringType

/**
 * Base class for option-based widgets (like Dropdowns, Select, etc.).
 *
 * @param ValueT The type of a single option's value.
 * @param ValueSelectionT The type representing the current selection (could be single value, list, pair, etc.).
 * @param IndexSelectionT The type representing the frontend index selection (could be Int, List<Int>, IntRange, etc.).
 */
public abstract class OptionWidgetBase<ValueT, ValueSelectionT, IndexSelectionT>(
    spec: WidgetSpec,
    widgetManager: WidgetManager,
) : DefaultWidgetModel(spec, widgetManager) {
    /**
     * The labels for the options displayed in the UI.
     * Maps to `_options_labels` on the frontend.
     */
    public var optionsLabels: List<String> by prop(
        "_options_labels",
        ArrayType(
            StringType,
        ),
        emptyList(),
    )

    /**
     * Internal storage for option values.
     * Frontend only knows about labels and indices; values are kept on the backend.
     */
    protected var optionsValues: List<ValueT>? = null

    /**
     * Selected index(es). This is what's actually synced with the frontend's `index` property.
     */
    public abstract var index: IndexSelectionT

    /**
     * The options to choose from as a list of label-to-value pairs.
     * Setting this updates [optionsLabels] and internal [optionsValues].
     *
     * IMPORTANT: When options change, the implementation tries to preserve the current [value]
     * by looking it up in the new options list.
     */
    public var options: List<Pair<String, ValueT>>
        get() = optionsLabels.zip(optionsValues ?: optionsLabels.map(::labelToValue))
        set(value) {
            val currentValue = this.value
            optionsLabels = value.map { it.first }
            optionsValues = value.map { it.second }
            // Try to maintain selection if the current value exists in the new options
            index = findIndexByValue(currentValue)
        }

    /**
     * Convenience property to set options when labels and values are identical.
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
     * The current selection represented as a value (or values).
     * This is a high-level abstraction over the [index] property.
     */
    public abstract var value: ValueSelectionT

    /**
     * Finds the index corresponding to the given value selection.
     */
    protected abstract fun findIndexByValue(v: ValueSelectionT): IndexSelectionT

    /**
     * Provides a default value for a label when [optionsValues] is not explicitly set.
     */
    protected abstract fun labelToValue(label: String): ValueT
}
