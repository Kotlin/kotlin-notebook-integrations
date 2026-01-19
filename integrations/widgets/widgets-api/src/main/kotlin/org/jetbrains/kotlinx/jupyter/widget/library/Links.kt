package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.getProperty
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
    val sourcePropertyName = sourceWidget.getProperty(sourceProperty)?.name ?: error("Source property not found")
    val targetPropertyName = targetWidget.getProperty(targetProperty)?.name ?: error("Target property not found")

    return link {
        source = sourceWidget to sourcePropertyName
        target = targetWidget to targetPropertyName
    }
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
    val sourcePropertyName = sourceWidget.getProperty(sourceProperty)?.name ?: error("Source property not found")
    val targetPropertyName = targetWidget.getProperty(targetProperty)?.name ?: error("Target property not found")

    return directionalLink {
        source = sourceWidget to sourcePropertyName
        target = targetWidget to targetPropertyName
    }
}
