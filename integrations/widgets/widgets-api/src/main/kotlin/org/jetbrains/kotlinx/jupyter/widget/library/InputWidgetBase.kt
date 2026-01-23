package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetSpec

/**
 * Base class for Input widgets.
 */
public abstract class InputWidgetBase(
    spec: WidgetSpec,
    widgetManager: WidgetManager,
    fromFrontend: Boolean,
) : DomWidgetBase(spec, widgetManager, fromFrontend),
    WidgetWithDescription {
    public open var allowDuplicates: Boolean by boolProp("allow_duplicates", true)

    /**
     * Description of the control.
     */
    public override var description: String by stringProp("description", "")

    /**
     * Accept HTML in the description.
     */
    public open var descriptionAllowHtml: Boolean by boolProp("description_allow_html", false)

    public open var placeholder: String by stringProp("placeholder", "​")

    /**
     * Styling customizations
     */
    public open var style: DescriptionStyleWidget? by nullableWidgetProp(
        "style",
        if (fromFrontend) null else widgetManager.descriptionStyle(),
    )
}
