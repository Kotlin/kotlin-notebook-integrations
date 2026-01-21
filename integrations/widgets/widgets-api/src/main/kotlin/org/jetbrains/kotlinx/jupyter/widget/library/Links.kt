package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.getProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * Creates a [LinkWidget] to link specified properties of two widgets.
 * Created link is bidirectional.
 */
public fun <SourceT : WidgetModel, TargetT : WidgetModel> WidgetManager.linkProperties(
    sourceWidget: SourceT,
    sourceProperty: KProperty1<SourceT, *>,
    targetWidget: TargetT,
    targetProperty: KProperty1<TargetT, *>,
): LinkWidget {
    val sourcePropertyName = sourceWidget.getPropertyNameOrThrow(sourceProperty, "Source")
    val targetPropertyName = targetWidget.getPropertyNameOrThrow(targetProperty, "Target")
    return linkProperties(sourceWidget, sourcePropertyName, targetWidget, targetPropertyName)
}

/**
 * Creates a [LinkWidget] to link specified properties of two widgets.
 * Created link is bidirectional.
 */
public fun <SourceT : WidgetModel, TargetT : WidgetModel> WidgetManager.linkProperties(
    sourceWidget: SourceT,
    sourcePropertyProvider: SourceT.() -> KProperty0<*>,
    targetWidget: TargetT,
    targetPropertyProvider: TargetT.() -> KProperty0<*>,
): LinkWidget {
    val sourcePropertyName = sourceWidget.getPropertyNameOrThrow(sourcePropertyProvider, "Source")
    val targetPropertyName = targetWidget.getPropertyNameOrThrow(targetPropertyProvider, "Target")
    return linkProperties(sourceWidget, sourcePropertyName, targetWidget, targetPropertyName)
}

/**
 * Creates a [LinkWidget] to link specified properties of two widgets.
 * Created link is one-way from source to target.
 */
public fun <SourceT : WidgetModel, TargetT : WidgetModel> WidgetManager.linkPropertiesOneWay(
    sourceWidget: SourceT,
    sourceProperty: KProperty1<SourceT, *>,
    targetWidget: TargetT,
    targetProperty: KProperty1<TargetT, *>,
): DirectionalLinkWidget {
    val sourcePropertyName = sourceWidget.getPropertyNameOrThrow(sourceProperty, "Source")
    val targetPropertyName = targetWidget.getPropertyNameOrThrow(targetProperty, "Target")
    return linkPropertiesOneWay(sourceWidget, sourcePropertyName, targetWidget, targetPropertyName)
}

/**
 * Creates a [LinkWidget] to link specified properties of two widgets.
 * Created link is one-way from source to target.
 */
public fun <SourceT : WidgetModel, TargetT : WidgetModel> WidgetManager.linkPropertiesOneWay(
    sourceWidget: SourceT,
    sourcePropertyProvider: SourceT.() -> KProperty0<*>,
    targetWidget: TargetT,
    targetPropertyProvider: TargetT.() -> KProperty0<*>,
): DirectionalLinkWidget {
    val sourcePropertyName = sourceWidget.getPropertyNameOrThrow(sourcePropertyProvider, "Source")
    val targetPropertyName = targetWidget.getPropertyNameOrThrow(targetPropertyProvider, "Target")
    return linkPropertiesOneWay(sourceWidget, sourcePropertyName, targetWidget, targetPropertyName)
}

private fun <SourceT : WidgetModel, TargetT : WidgetModel> WidgetManager.linkProperties(
    sourceWidget: SourceT,
    sourcePropertyName: String,
    targetWidget: TargetT,
    targetPropertyName: String,
): LinkWidget =
    link {
        source = sourceWidget to sourcePropertyName
        target = targetWidget to targetPropertyName
    }

private fun <SourceT : WidgetModel, TargetT : WidgetModel> WidgetManager.linkPropertiesOneWay(
    sourceWidget: SourceT,
    sourcePropertyName: String,
    targetWidget: TargetT,
    targetPropertyName: String,
): DirectionalLinkWidget =
    directionalLink {
        source = sourceWidget to sourcePropertyName
        target = targetWidget to targetPropertyName
    }

private fun <T : WidgetModel> T.getPropertyNameOrThrow(
    property: KProperty1<T, *>,
    widgetKind: String,
): String = getProperty(property)?.name ?: throwPropertyNotFound(widgetKind)

private fun <T : WidgetModel> T.getPropertyNameOrThrow(
    propertyGetter: T.() -> KProperty0<*>,
    widgetKind: String,
): String {
    val property = propertyGetter()
    return getProperty(property)?.name ?: throwPropertyNotFound(widgetKind)
}

private fun throwPropertyNotFound(widgetKind: String): Nothing {
    error("$widgetKind property not found")
}
