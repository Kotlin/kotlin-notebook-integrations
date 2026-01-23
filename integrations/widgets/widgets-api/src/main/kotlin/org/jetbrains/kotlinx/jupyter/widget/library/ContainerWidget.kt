package org.jetbrains.kotlinx.jupyter.widget.library

import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel

/**
 * Interface for widgets that can contain other widgets.
 */
public interface ContainerWidget {
    /**
     * List of widget children
     */
    public var children: List<WidgetModel?>
}
