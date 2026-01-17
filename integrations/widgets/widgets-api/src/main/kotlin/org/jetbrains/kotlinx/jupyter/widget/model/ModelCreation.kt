package org.jetbrains.kotlinx.jupyter.widget.model

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager

/**
 * Creates a widget instance and registers it with the [WidgetManager].
 */
public fun <M : WidgetModel> WidgetManager.createAndRegisterWidget(widgetFactory: (widgetManager: WidgetManager) -> M): M =
    widgetFactory(this).also { widget -> registerWidget(widget) }

/**
 * Creates a widget instance using the provided [factory] and registers it.
 */
public fun <M : WidgetModel> WidgetManager.createAndRegisterWidget(factory: WidgetFactory<M>): M = createAndRegisterWidget(factory::create)

/**
 * Factory for creating [WidgetModel] instances.
 */
public interface WidgetFactory<M : WidgetModel> {
    /**
     * The specification of the widget this factory creates.
     */
    public val spec: WidgetSpec

    /**
     * Creates a new widget instance.
     * @param fromFrontend If true, indicates the widget is being reconstructed from frontend state.
     */
    public fun create(
        widgetManager: WidgetManager,
        fromFrontend: Boolean = false,
    ): M
}

/**
 * Default implementation of [WidgetFactory] that uses a provided constructor function.
 */
public abstract class DefaultWidgetFactory<M : DefaultWidgetModel>(
    override val spec: WidgetSpec,
    private val factory: (widgetManager: WidgetManager, fromFrontend: Boolean) -> M,
) : WidgetFactory<M> {
    /**
     * Creates a new widget instance using the provided constructor.
     */
    override fun create(
        widgetManager: WidgetManager,
        fromFrontend: Boolean,
    ): M = factory(widgetManager, fromFrontend)
}

/**
 * A [WidgetModel] that automatically includes standard [WidgetSpec] properties as synced properties.
 */
public open class DefaultWidgetModel(
    spec: WidgetSpec,
    widgetManager: WidgetManager,
) : WidgetModel(widgetManager) {
    /**
     * The name of the frontend model class.
     */
    public val modelName: String by stringProp("_model_name", spec.modelName)

    /**
     * The NPM module containing the model.
     */
    public val modelModule: String by stringProp("_model_module", spec.modelModule)

    /**
     * Semver requirement for the model module.
     */
    public val modelModuleVersion: String by stringProp("_model_module_version", spec.modelModuleVersion)

    /**
     * The name of the frontend view class.
     */
    public val viewName: String? by nullableStringProp("_view_name", spec.viewName)

    /**
     * The NPM module containing the view.
     */
    public val viewModule: String by stringProp("_view_module", spec.viewModule)

    /**
     * Semver requirement for the view module.
     */
    public val viewModuleVersion: String by stringProp("_view_module_version", spec.viewModuleVersion)
}
