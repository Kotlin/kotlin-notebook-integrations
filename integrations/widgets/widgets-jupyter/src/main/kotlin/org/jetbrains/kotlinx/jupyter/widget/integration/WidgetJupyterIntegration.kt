package org.jetbrains.kotlinx.jupyter.widget.integration

import org.jetbrains.kotlinx.jupyter.api.declare
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.createWidgetManager
import org.jetbrains.kotlinx.jupyter.widget.library.DatePickerWidget
import org.jetbrains.kotlinx.jupyter.widget.library.enums.BoxStyle
import org.jetbrains.kotlinx.jupyter.widget.library.links.LinkWidgetBase
import org.jetbrains.kotlinx.jupyter.widget.library.media.MediaWidget
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel
import java.time.LocalDate

private var myWidgetManager: WidgetManager? = null
internal val globalWidgetManager: WidgetManager get() = myWidgetManager!!

public class WidgetJupyterIntegration : JupyterIntegration() {
    override fun Builder.onLoaded() {
        importPackage<WidgetJupyterIntegration>()
        importPackage<DatePickerWidget>()
        importPackage<MediaWidget>()
        importPackage<LinkWidgetBase>()
        importPackage<WidgetModel>()
        importPackage<LocalDate>()
        importPackage<BoxStyle>()

        var myLastClassLoader = WidgetJupyterIntegration::class.java.classLoader
        val widgetManager = createWidgetManager(notebook) { myLastClassLoader }
        myWidgetManager = widgetManager

        onLoaded {
            myLastClassLoader = this.lastClassLoader

            declare("widgetManager" to widgetManager)
        }

        afterCellExecution { _, _ ->
            myLastClassLoader = this.lastClassLoader
        }

        renderWithHost<WidgetModel> { _, widget ->
            widgetManager.renderWidget(widget)
        }
    }
}
