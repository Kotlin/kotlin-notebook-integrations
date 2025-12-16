package org.jetbrains.kotlinx.jupyter.widget.model.types

public abstract class AbstractWidgetModelPropertyType<T>(
    override val name: String,
) : WidgetModelPropertyType<T> {
    override fun toString(): String = name
}
