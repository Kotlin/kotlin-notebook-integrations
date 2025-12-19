package org.jetbrains.kotlinx.jupyter.widget.integration

import org.jetbrains.kotlinx.jupyter.widget.library.HtmlWidget
import org.jetbrains.kotlinx.jupyter.widget.library.IntSliderWidget
import org.jetbrains.kotlinx.jupyter.widget.library.LabelWidget
import org.jetbrains.kotlinx.jupyter.widget.library.LayoutWidget
import org.jetbrains.kotlinx.jupyter.widget.library.OutputWidget
import org.jetbrains.kotlinx.jupyter.widget.library.html
import org.jetbrains.kotlinx.jupyter.widget.library.intSlider
import org.jetbrains.kotlinx.jupyter.widget.library.label
import org.jetbrains.kotlinx.jupyter.widget.library.layout
import org.jetbrains.kotlinx.jupyter.widget.library.output

public fun htmlWidget(): HtmlWidget = globalWidgetManager.html()

public fun intSliderWidget(): IntSliderWidget = globalWidgetManager.intSlider()

public fun labelWidget(): LabelWidget = globalWidgetManager.label()

public fun layoutWidget(): LayoutWidget = globalWidgetManager.layout()

public fun outputWidget(): OutputWidget = globalWidgetManager.output()
