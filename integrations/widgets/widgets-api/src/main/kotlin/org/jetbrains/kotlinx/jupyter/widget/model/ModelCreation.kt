package org.jetbrains.kotlinx.jupyter.widget.model

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import java.util.ServiceLoader
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

private val factoryCache = ConcurrentHashMap<String, WidgetFactory<*>>()

internal fun loadWidgetFactory(
    modelName: String,
    classLoader: ClassLoader,
): WidgetFactory<*> =
    factoryCache.getOrPut(modelName) {
        ServiceLoader
            .load(WidgetFactory::class.java, classLoader)
            .firstOrNull { it.spec.modelName == modelName } ?: error("No factory for model $modelName")
    }

public fun <M : WidgetModel> WidgetManager.createAndRegisterWidget(widgetFactory: () -> M): M =
    widgetFactory().also { widget -> registerWidget(widget) }

public inline fun <reified F : WidgetFactory<M>, M : WidgetModel> WidgetManager.createAndRegisterWidget(factoryKClass: KClass<F>): M {
    @Suppress("UNCHECKED_CAST")
    val factory =
        factoryKClass.java
            .getDeclaredConstructor()
            .apply { isAccessible = true }
            .newInstance() as WidgetFactory<M>

    return createAndRegisterWidget(factory::create)
}

public interface WidgetFactory<M : WidgetModel> {
    public val spec: WidgetSpec

    public fun create(): M
}

public abstract class DefaultWidgetFactory<M : DefaultWidgetModel>(
    override val spec: WidgetSpec,
    private val factory: (spec: WidgetSpec) -> M,
) : WidgetFactory<M> {
    override fun create(): M = factory(spec)
}

public open class DefaultWidgetModel(
    spec: WidgetSpec,
) : WidgetModel() {
    public val modelName: String by stringProp("_model_name", spec.modelName)
    public val modelModule: String by stringProp("_model_module", spec.modelModule)
    public val modelModuleVersion: String by stringProp("_model_module_version", spec.modelModuleVersion)
    public val viewName: String by stringProp("_view_name", spec.viewName)
    public val viewModule: String by stringProp("_view_module", spec.viewModule)
    public val viewModuleVersion: String by stringProp("_view_module_version", spec.viewModuleVersion)
}
