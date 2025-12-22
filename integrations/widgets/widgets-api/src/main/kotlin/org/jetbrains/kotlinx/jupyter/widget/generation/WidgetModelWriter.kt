package org.jetbrains.kotlinx.jupyter.widget.generation

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.text.appendLine

public class WidgetModelWriter {
    public fun write(
        models: List<WidgetModelDescription>,
        outputFile: Path,
    ): Unit =
        outputFile
            .also { it.parent?.createDirectories() }
            .writeText(renderFile(models))

    private fun renderFile(models: List<WidgetModelDescription>): String =
        buildString {
            appendLine("@file:Suppress(\"RedundantVisibilityModifier\", \"SpellCheckingInspection\")")
            appendLine("package org.jetbrains.kotlinx.jupyter.widget.generation.generated")
            appendLine()
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.generation.WidgetModelDescription")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.generation.WidgetPropertyDescription")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.generation.WidgetSchemaTypeDescription")
            appendLine()
            appendLine("public val generatedWidgetModels: List<WidgetModelDescription> = listOf(")
            models.forEachIndexed { index, model ->
                if (index > 0) {
                    appendLine(",")
                }
                append("    WidgetModelDescription(")
                appendLine()
                append("        name = \"").append(model.name).appendLine("\",")
                appendLine("        properties = listOf(")
                model.properties.forEachIndexed { propIndex, property ->
                    if (propIndex > 0) {
                        appendLine(",")
                    }
                    append("            WidgetPropertyDescription(")
                    appendLine()
                    append("                name = \"").append(property.name).appendLine("\",")
                    append("                type = WidgetSchemaTypeDescription.")
                        .append(property.type.name)
                        .appendLine(",")
                    append("                required = ")
                        .append(property.required.toString())
                        .appendLine(",")
                    val escapedSchema =
                        property.rawSchema
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                    append("                rawSchema = \"")
                        .append(escapedSchema)
                        .appendLine("\",")
                    append("            )")
                }
                if (model.properties.isNotEmpty()) {
                    appendLine()
                }
                appendLine("        ),")
                append("    )")
            }
            if (models.isNotEmpty()) {
                appendLine()
            }
            appendLine(")")
        }
}
