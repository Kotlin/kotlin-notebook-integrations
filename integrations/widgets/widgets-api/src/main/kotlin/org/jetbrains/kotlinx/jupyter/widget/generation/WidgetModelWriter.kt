package org.jetbrains.kotlinx.jupyter.widget.generation

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.text.appendLine
import kotlinx.serialization.json.JsonPrimitive

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
            appendLine("package org.jetbrains.kotlinx.jupyter.widget.library")
            appendLine()
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.WidgetManager")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetFactory")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.model.baseSpec")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.model.controlsSpec")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.model.createAndRegisterWidget")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.model.iPyWidgetsSpec")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.model.outputSpec")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.NullableType")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.AnyType")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.BooleanType")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.FloatType")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.IntType")
            appendLine("import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.StringType")
            appendLine()

            models.forEachIndexed { index, model ->
                if (index > 0) appendLine()
                appendLine(renderModel(model))
            }
        }

    private fun renderModel(model: WidgetModelDescription): String =
        buildString {
            val specName = model.controlName.replaceFirstChar { it.lowercaseChar() } + "Spec"
            append("private val ").append(specName).append(" = ")
            appendLine(renderSpec(model))
            appendLine()
            append("public fun WidgetManager.")
                .append(model.controlName.replaceFirstChar { it.lowercaseChar() })
                .append("(): ")
                .append(model.className())
                .append(" = createAndRegisterWidget(")
                .append(model.className())
                .appendLine(".Factory)")
            appendLine()
            append("public class ")
                .append(model.className())
                .append(" internal constructor(\n    widgetManager: WidgetManager,\n) : DefaultWidgetModel(")
                .append(specName)
                .appendLine(", widgetManager) {")
            append("    internal object Factory : DefaultWidgetFactory<")
                .append(model.className())
                .append(")(")
                .append(specName)
                .append(", ::")
                .append(model.className())
                .appendLine(")")
            appendLine()

            model.properties.forEach { property ->
                appendLine("    " + renderProperty(property))
            }
            appendLine("}")
        }

    private fun renderSpec(model: WidgetModelDescription): String {
        val spec = model.spec
        val controlName = model.controlName
        return when (spec.modelModule) {
            "@jupyter-widgets/controls" ->
                "controlsSpec(\"$controlName\", \"${spec.modelModuleVersion}\")"
            "@jupyter-widgets/base" ->
                "baseSpec(\"$controlName\", \"${spec.modelModuleVersion}\")"
            "@jupyter-widgets/output" ->
                "outputSpec(\"$controlName\", \"${spec.modelModuleVersion}\")"
            else ->
                "iPyWidgetsSpec(\"$controlName\", \"${spec.modelModule}\", \"${spec.modelModuleVersion}\")"
        }
    }

    private fun renderProperty(property: WidgetPropertyDescription): String {
        val name = property.name
        val (kotlinType, delegate) = when (property.type) {
            WidgetSchemaTypeDescription.STRING -> stringProperty(property)
            WidgetSchemaTypeDescription.INTEGER -> intProperty(property)
            WidgetSchemaTypeDescription.NUMBER -> floatProperty(property)
            WidgetSchemaTypeDescription.BOOLEAN -> boolProperty(property)
            WidgetSchemaTypeDescription.REFERENCE -> referenceProperty(property)
            else -> anyProperty(property)
        }

        return buildString {
            append("public var ").append(name).append(": ").append(kotlinType)
            append(" by ").append(delegate)
        }
    }

    private fun stringProperty(property: WidgetPropertyDescription): Pair<String, String> =
        if (property.required) {
            "String" to "stringProp(\"${property.name}\", ${property.defaultString("\"\"")})"
        } else {
            "String?" to "prop(\"${property.name}\", NullableType(StringType), ${property.defaultString("null")})"
        }

    private fun intProperty(property: WidgetPropertyDescription): Pair<String, String> =
        if (property.required) {
            "Int" to "intProp(\"${property.name}\", ${property.defaultString("0")})"
        } else {
            "Int?" to "prop(\"${property.name}\", NullableType(IntType), ${property.defaultString("null")})"
        }

    private fun floatProperty(property: WidgetPropertyDescription): Pair<String, String> =
        if (property.required) {
            "Double" to "doubleProp(\"${property.name}\", ${property.defaultString("0.0")})"
        } else {
            "Double?" to "prop(\"${property.name}\", NullableType(FloatType), ${property.defaultString("null")})"
        }

    private fun boolProperty(property: WidgetPropertyDescription): Pair<String, String> =
        if (property.required) {
            "Boolean" to "boolProp(\"${property.name}\", ${property.defaultString("false")})"
        } else {
            "Boolean?" to "prop(\"${property.name}\", NullableType(BooleanType), ${property.defaultString("null")})"
        }

    private fun referenceProperty(property: WidgetPropertyDescription): Pair<String, String> {
        val refName = property.ref?.substringAfterLast('/')?.removeSuffix("Model") ?: "WidgetModel"
        val widgetClass = refName + "Widget"
        return widgetClass + "?" to "widgetProp(\"${property.name}\")"
    }

    private fun anyProperty(property: WidgetPropertyDescription): Pair<String, String> =
        "Any?" to "prop(\"${property.name}\", AnyType, null)"

    private fun WidgetModelDescription.className(): String = controlName + "Widget"

    private fun WidgetPropertyDescription.defaultString(fallback: String): String {
        val default = defaultValue ?: return fallback
        if (default is JsonPrimitive) {
            if (default.isString) return "\"" + default.content + "\""
            return default.content
        }
        return fallback
    }
}
