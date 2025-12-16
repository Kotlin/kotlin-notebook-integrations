package org.jetbrains.kotlinx.jupyter.widget.model.types.widget

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType

private const val WIDGET_REF_PREFIX = "IPY_MODEL_"

public class WidgetReferenceType<M : WidgetModel> : AbstractWidgetModelPropertyType<M?>("widget-ref") {
    override val default: M? get() = null

    override fun serialize(propertyValue: M?): String? =
        propertyValue?.let {
            "$WIDGET_REF_PREFIX${it.id}"
        }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager?,
    ): M? {
        if (patchValue == null) return null

        requireNotNull(widgetManager) {
            "Widget manager is required to deserialize widget-ref"
        }
        require(patchValue is String) {
            "Expected String for widget-ref, got ${patchValue::class.simpleName}"
        }
        require(patchValue.startsWith(WIDGET_REF_PREFIX)) {
            "Invalid widget ref format: $patchValue"
        }
        val id = patchValue.removePrefix(WIDGET_REF_PREFIX)
        val model =
            widgetManager.getWidget(id)
                ?: error("Widget with id=$id not found")
        return model as M
    }
}
