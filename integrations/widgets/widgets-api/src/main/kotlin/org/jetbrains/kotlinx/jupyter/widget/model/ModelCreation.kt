package org.jetbrains.kotlinx.jupyter.widget.model

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager

public fun <M : WidgetModel> WidgetManager.createAndRegisterWidget(widgetFactory: (widgetManager: WidgetManager) -> M): M =
    widgetFactory(this).also { widget -> registerWidget(widget) }

public fun <M : WidgetModel> WidgetManager.createAndRegisterWidget(factory: WidgetFactory<M>): M = createAndRegisterWidget(factory::create)

public interface WidgetFactory<M : WidgetModel> {
    public val spec: WidgetSpec

    public fun create(widgetManager: WidgetManager): M
}

public abstract class DefaultWidgetFactory<M : DefaultWidgetModel>(
    override val spec: WidgetSpec,
    private val factory: (widgetManager: WidgetManager) -> M,
) : WidgetFactory<M> {
    public constructor(spec: WidgetSpec, factory: () -> M) :
        this(spec, { _ -> factory() })

    override fun create(widgetManager: WidgetManager): M = factory(widgetManager)
}

public open class DefaultWidgetModel(
    spec: WidgetSpec,
    widgetManager: WidgetManager,
) : WidgetModel(widgetManager) {
    public val modelName: String by stringProp("_model_name", spec.modelName)
    public val modelModule: String by stringProp("_model_module", spec.modelModule)
    public val modelModuleVersion: String by stringProp("_model_module_version", spec.modelModuleVersion)
    public val viewName: String by stringProp("_view_name", spec.viewName)
    public val viewModule: String by stringProp("_view_module", spec.viewModule)
    public val viewModuleVersion: String by stringProp("_view_module_version", spec.viewModuleVersion)
}
