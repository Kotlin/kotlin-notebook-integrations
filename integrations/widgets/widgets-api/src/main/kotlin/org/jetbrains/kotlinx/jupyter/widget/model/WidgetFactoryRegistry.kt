package org.jetbrains.kotlinx.jupyter.widget.model

import org.jetbrains.kotlinx.jupyter.widget.library.extraWidgetFactories
import org.jetbrains.kotlinx.jupyter.widget.library.registry.defaultWidgetFactories
import java.util.ServiceLoader
import java.util.concurrent.ConcurrentHashMap

/**
 * Keeps track of available [WidgetFactory] instances.
 * Factories are used to reconstruct widgets from the state received from the frontend.
 */
public class WidgetFactoryRegistry {
    private val factoryCache = ConcurrentHashMap<String, WidgetFactory<*>>()

    init {
        // Load factories provided by the library and extra ones added via registration
        for (factory in extraWidgetFactories) {
            registerWidgetFactory(factory)
        }
        for (factory in defaultWidgetFactories) {
            registerWidgetFactory(factory)
        }
    }

    /**
     * Finds a factory for the given [modelName].
     * If not found in cache, tries to load it using [ServiceLoader].
     */
    internal fun loadWidgetFactory(
        modelName: String,
        classLoader: ClassLoader,
    ): WidgetFactory<*> =
        factoryCache.getOrPut(modelName) {
            ServiceLoader
                .load(WidgetFactory::class.java, classLoader)
                .firstOrNull { it.spec.modelName == modelName } ?: error("No factory for model $modelName")
        }

    /**
     * Registers a new widget factory.
     */
    public fun registerWidgetFactory(factory: WidgetFactory<*>) {
        factoryCache[factory.spec.modelName] = factory
    }
}
