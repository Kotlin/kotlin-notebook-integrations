package org.jetbrains.kotlinx.jupyter.widget.model.types.widget

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue

private const val WIDGET_REF_PREFIX = "IPY_MODEL_"

/**
 * Property type for referencing other widgets.
 * In the Jupyter protocol, widget references are strings prefixed with "IPY_MODEL_".
 */
public class WidgetReferenceType<M : WidgetModel> : AbstractWidgetModelPropertyType<M>("widget-ref") {
    /**
     * References do not have a sensible default value.
     */
    override val default: M get() = error("No default value for widget-ref")

    /**
     * Serializes a [WidgetModel] into its string ID representation.
     */
    override fun serialize(
        propertyValue: M,
        widgetManager: WidgetManager,
    ): RawPropertyValue {
        val widgetId =
            widgetManager.getWidgetId(propertyValue)
                ?: error("Widget id for widget $propertyValue was not found")
        return RawPropertyValue.StringValue("$WIDGET_REF_PREFIX$widgetId")
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(
        patchValue: RawPropertyValue,
        widgetManager: WidgetManager,
    ): M {
        require(patchValue is RawPropertyValue.StringValue) {
            "Expected WidgetValue.StringValue for widget-ref, got ${patchValue::class.simpleName}"
        }
        val value = patchValue.value
        require(value.startsWith(WIDGET_REF_PREFIX)) {
            "Invalid widget ref format: $value"
        }
        val id = value.removePrefix(WIDGET_REF_PREFIX)
        val model =
            widgetManager.getWidget(id)
                ?: error("Widget with id=$id not found")
        return model as M
    }
}
