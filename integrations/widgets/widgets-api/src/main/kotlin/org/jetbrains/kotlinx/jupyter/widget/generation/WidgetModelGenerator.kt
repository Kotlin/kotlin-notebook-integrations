@file:Suppress("ktlint:standard:import-ordering")

package org.jetbrains.kotlinx.jupyter.widget.generation

import java.io.InputStream
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

public class WidgetModelGenerator(
    private val schemaParser: WidgetModelSchemaParser = WidgetModelSchemaParser(defaultJson),
    private val writer: WidgetModelWriter = WidgetModelWriter(),
) {
    public fun generateFromUrl(
        outputDir: Path,
        schemaUrl: String = DEFAULT_SCHEMA_URL,
    ): Path {
        URL(schemaUrl).openStream().use { stream ->
            return generate(stream, outputDir)
        }
    }

    public fun generateFromFile(
        schemaFile: Path,
        outputDir: Path,
    ): Path = schemaFile.inputStream().use { stream -> generate(stream, outputDir) }

    public fun generate(
        schemaStream: InputStream,
        outputDir: Path,
    ): Path =
        schemaStream.bufferedReader().use { reader ->
            val root = defaultJson.parseToJsonElement(reader.readText())
            val schema = root.asSchemaObject()
            generate(schema, outputDir)
        }

    public fun generate(
        schema: JsonObject,
        outputDir: Path,
    ): Path {
        val models = schemaParser.parse(schema)
        val outputFile = outputDir.resolve(DEFAULT_OUTPUT_FILE)
        writer.write(models, outputFile)
        return outputFile
    }

    public companion object {
        public const val DEFAULT_SCHEMA_URL: String =
            "https://raw.githubusercontent.com/jupyter-widgets/ipywidgets/refs/heads/main/packages/schema/jupyterwidgetmodels.latest.json"
        public const val DEFAULT_OUTPUT_FILE: String = "GeneratedWidgetModels.kt"
        internal val defaultJson: Json =
            Json {
                ignoreUnknownKeys = true
                prettyPrint = false
            }

        private fun JsonElement.asSchemaObject(): JsonObject =
            when (this) {
                is JsonObject -> this
                is JsonArray -> firstNotNullOfOrNull { element ->
                    element.asSchemaObjectOrNull()
                }
                    ?: error("Schema root must be a JsonObject or contain an object with 'definitions'")
            }

        private fun JsonElement.asSchemaObjectOrNull(): JsonObject? =
            when (this) {
                is JsonObject -> takeIf { "definitions" in this }
                is JsonArray -> firstNotNullOfOrNull { element -> element.asSchemaObjectOrNull() }
                else -> null
            }
    }
}
