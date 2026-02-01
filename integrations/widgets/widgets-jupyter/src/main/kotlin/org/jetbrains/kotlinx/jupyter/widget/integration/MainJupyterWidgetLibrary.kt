package org.jetbrains.kotlinx.jupyter.widget.integration

import org.jetbrains.kotlinx.jupyter.widget.library.ButtonWidget
import org.jetbrains.kotlinx.jupyter.widget.library.OutputWidget
import org.jetbrains.kotlinx.jupyter.widget.library.button
import org.jetbrains.kotlinx.jupyter.widget.library.output

/**
 * Creates an [OutputWidget] and registers it with the global widget manager.
 *
 * @param setup A lambda to configure the widget after creation.
 * @return The created and registered [OutputWidget].
 */
public fun outputWidget(setup: OutputWidget.() -> Unit = {}): OutputWidget = globalWidgetManager.output(setup)

/**
 * Creates an [ButtonWidget] and registers it with the global widget manager.
 *
 * @param setup A lambda to configure the widget after creation.
 * @return The created and registered [ButtonWidget].
 */
public fun buttonWidget(setup: ButtonWidget.() -> Unit = {}): ButtonWidget = globalWidgetManager.button(setup)
