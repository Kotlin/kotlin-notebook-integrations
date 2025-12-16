package org.jetbrains.kotlinx.jupyter.widget.model.types.enums

import kotlin.properties.ReadOnlyProperty

public abstract class WidgetEnum<T : WidgetEnum<T>> {
    private val _entries = mutableListOf<WidgetEnumEntry<T>>()
    public val entries: List<WidgetEnumEntry<T>> get() = _entries

    protected fun entry(name: String): ReadOnlyProperty<WidgetEnum<T>, WidgetEnumEntry<T>> {
        val e = WidgetEnumEntry<T>(name)
        _entries.add(e)
        return ReadOnlyProperty { _, _ -> e }
    }
}
