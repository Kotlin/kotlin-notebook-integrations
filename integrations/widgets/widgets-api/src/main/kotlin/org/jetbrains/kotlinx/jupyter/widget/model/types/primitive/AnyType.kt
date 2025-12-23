package org.jetbrains.kotlinx.jupyter.widget.model.types.primitive

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType

public object AnyType : AbstractWidgetModelPropertyType<Any?>("any") {
    override val default: Any? = null

    override fun serialize(
        propertyValue: Any?,
        widgetManager: WidgetManager,
    ): Any? = propertyValue

    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager,
    ): Any? = patchValue
}
