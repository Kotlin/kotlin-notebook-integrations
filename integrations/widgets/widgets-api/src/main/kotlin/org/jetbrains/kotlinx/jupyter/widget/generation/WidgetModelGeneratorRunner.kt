package org.jetbrains.kotlinx.jupyter.widget.generation

import java.nio.file.Path
import kotlin.io.path.Path

public object WidgetModelGeneratorRunner {
    @JvmStatic
    public fun main(args: Array<String>) {
        val outputDir = args.getOrNull(0)?.let(::Path) ?: defaultOutputDir()
        val generator = WidgetModelGenerator()
        generator.generateFromUrl(outputDir)
    }

    private fun defaultOutputDir(): Path = Path("build/generated/widgetModels")
}
