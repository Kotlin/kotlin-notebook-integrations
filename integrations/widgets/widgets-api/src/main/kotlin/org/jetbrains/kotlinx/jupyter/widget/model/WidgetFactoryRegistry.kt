package org.jetbrains.kotlinx.jupyter.widget.model

import org.jetbrains.kotlinx.jupyter.widget.library.registry.defaultWidgetFactories
import java.util.ServiceLoader
import java.util.concurrent.ConcurrentHashMap

public class WidgetFactoryRegistry {
    private val factoryCache = ConcurrentHashMap<String, WidgetFactory<*>>()

    init {
        for (factory in defaultWidgetFactories) {
            registerWidgetFactory(factory)
        }
    }

    internal fun loadWidgetFactory(
        modelName: String,
        classLoader: ClassLoader,
    ): WidgetFactory<*> =
        factoryCache.getOrPut(modelName) {
            ServiceLoader
                .load(WidgetFactory::class.java, classLoader)
                .firstOrNull { it.spec.modelName == modelName } ?: error("No factory for model $modelName")
        }

    public fun registerWidgetFactory(factory: WidgetFactory<*>) {
        factoryCache[factory.spec.modelName] = factory
    }
}
