package org.jetbrains.kotlinx.jupyter.widget.model.types

/**
 * Abstract base class for [WidgetModelPropertyType] implementations that
 * provides a default [toString] implementation.
 */
public abstract class AbstractWidgetModelPropertyType<T>(
    override val name: String,
) : WidgetModelPropertyType<T> {
    override fun toString(): String = name
}
