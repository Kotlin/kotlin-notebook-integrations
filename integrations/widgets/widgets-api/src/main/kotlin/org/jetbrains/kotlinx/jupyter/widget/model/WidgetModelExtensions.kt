package org.jetbrains.kotlinx.jupyter.widget.model

import kotlin.reflect.KCallable
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible

/**
 * Retrieves a property by the KProperty of the widget class instance.
 */
public fun <T : WidgetModel> T.getProperty(kProperty: KProperty0<*>): WidgetModelProperty<*>? =
    getPropertyByDelegate(kProperty.withAccessibility { getDelegate() })

/**
 * Retrieves a property by the KProperty of this class.
 */
public fun <T : WidgetModel> T.getProperty(kProperty: KProperty1<T, *>): WidgetModelProperty<*>? =
    getPropertyByDelegate(kProperty.withAccessibility { getDelegate(this@getProperty) })

private fun <T : KCallable<*>, R> T.withAccessibility(action: T.() -> R): R =
    if (isAccessible) {
        this.action()
    } else {
        isAccessible = true
        try {
            this.action()
        } finally {
            isAccessible = false
        }
    }
