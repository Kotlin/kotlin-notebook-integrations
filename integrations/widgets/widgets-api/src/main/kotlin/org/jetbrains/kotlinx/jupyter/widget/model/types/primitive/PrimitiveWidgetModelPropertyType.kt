package org.jetbrains.kotlinx.jupyter.widget.model.types.primitive

import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType

/**
 * Base class for simple primitive property types.
 */
public abstract class PrimitiveWidgetModelPropertyType<T>(
    name: String,
    override val default: T,
) : AbstractWidgetModelPropertyType<T>(name)
