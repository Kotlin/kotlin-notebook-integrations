package org.jetbrains.kotlinx.jupyter.widget.integration

import org.jetbrains.kotlinx.jupyter.widget.library.OutputWidget
import org.jetbrains.kotlinx.jupyter.widget.library.output

public fun outputWidget(): OutputWidget = globalWidgetManager.output()
