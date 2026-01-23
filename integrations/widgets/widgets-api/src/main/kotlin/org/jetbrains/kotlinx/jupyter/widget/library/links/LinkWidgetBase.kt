package org.jetbrains.kotlinx.jupyter.widget.library.links

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetSpec
import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.NullableType
import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.PairType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.StringType
import org.jetbrains.kotlinx.jupyter.widget.model.types.widget.WidgetReferenceType

/**
 * Base class for link widgets.
 */
public abstract class LinkWidgetBase(
    spec: WidgetSpec,
    widgetManager: WidgetManager,
    fromFrontend: Boolean,
) : DefaultWidgetModel(spec, widgetManager) {
    /**
     * The source (widget, 'trait_name') pair
     */
    public open var source: Pair<WidgetModel?, String>? by prop(
        "source",
        NullableType(PairType(NullableType(WidgetReferenceType<WidgetModel>()), StringType)),
        null to "",
    )

    /**
     * The target (widget, 'trait_name') pair
     */
    public open var target: Pair<WidgetModel?, String>? by prop(
        "target",
        NullableType(PairType(NullableType(WidgetReferenceType<WidgetModel>()), StringType)),
        null to "",
    )
}
