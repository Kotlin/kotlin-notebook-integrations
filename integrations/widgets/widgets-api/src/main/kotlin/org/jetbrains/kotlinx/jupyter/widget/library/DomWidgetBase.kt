package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetSpec
import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.ArrayType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.StringType

/**
 * Base class for DOM widgets.
 */
public abstract class DomWidgetBase(
    spec: WidgetSpec,
    widgetManager: WidgetManager,
    fromFrontend: Boolean,
) : DefaultWidgetModel(spec, widgetManager) {
    /**
     * CSS classes applied to widget DOM element
     */
    public open var domClasses: List<String> by prop("_dom_classes", ArrayType(StringType), emptyList())

    /**
     * Widget layout settings
     */
    public open var layout: LayoutWidget? by nullableWidgetProp("layout", if (fromFrontend) null else widgetManager.layout())

    /**
     * Is widget tabbable?
     */
    public open var tabbable: Boolean? by nullableBoolProp("tabbable", null)

    /**
     * A tooltip caption.
     */
    public open var tooltip: String? by nullableStringProp("tooltip", null)
}
