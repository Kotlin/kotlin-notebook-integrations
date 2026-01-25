package org.jetbrains.kotlinx.jupyter.widget

import org.jetbrains.kotlinx.jupyter.api.Notebook
import org.jetbrains.kotlinx.jupyter.widget.display.WidgetDisplayControllerImpl

public fun createWidgetManager(
    notebook: Notebook,
    classLoaderProvider: () -> ClassLoader,
): WidgetManager {
    val displayController = WidgetDisplayControllerImpl(notebook)
    return WidgetManagerImpl(
        commManager = notebook.commManager,
        displayController = displayController,
        classLoaderProvider = classLoaderProvider,
    )
}
