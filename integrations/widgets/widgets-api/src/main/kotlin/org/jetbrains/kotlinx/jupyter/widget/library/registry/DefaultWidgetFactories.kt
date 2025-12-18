package org.jetbrains.kotlinx.jupyter.widget.library.registry

import org.jetbrains.kotlinx.jupyter.widget.library.HtmlWidget
import org.jetbrains.kotlinx.jupyter.widget.library.IntSliderWidget
import org.jetbrains.kotlinx.jupyter.widget.library.LabelWidget
import org.jetbrains.kotlinx.jupyter.widget.library.LayoutWidget
import org.jetbrains.kotlinx.jupyter.widget.library.OutputWidget
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetFactory

internal val defaultWidgetFactories =
    listOf<WidgetFactory<*>>(
        HtmlWidget.Factory,
        IntSliderWidget.Factory,
        LabelWidget.Factory,
        LayoutWidget.Factory,
        OutputWidget.Factory,
    )
