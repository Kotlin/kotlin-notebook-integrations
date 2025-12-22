@file:Suppress("ktlint:standard:chain-method-continuation")

package org.jetbrains.kotlinx.jupyter.widget.generation

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

public class WidgetModelSchemaParser(
    private val json: Json,
) {
    public fun parse(schema: JsonObject): List<WidgetModelDescription> {
        val definitions = schema["definitions"]?.jsonObject ?: return emptyList()
        return definitions.mapNotNull { (name, element) ->
            parseDefinition(name, element)
        }
    }

    private fun parseDefinition(
        name: String,
        element: JsonElement,
    ): WidgetModelDescription? {
        val definition = element.jsonObject
        val properties = definition["properties"]?.jsonObject ?: return null
        val required = definition["required"]?.asStringSet()
        val parsedProperties =
            properties.map { (propertyName, propertyElement) ->
                parseProperty(propertyName, propertyElement, required)
            }
        return WidgetModelDescription(name, parsedProperties)
    }

    private fun parseProperty(
        name: String,
        element: JsonElement,
        required: Set<String>?,
    ): WidgetPropertyDescription {
        val description = parseType(element.jsonObject)
        val raw = json.encodeToString(JsonElement.serializer(), element)
        return WidgetPropertyDescription(
            name = name,
            type = description,
            required = required?.contains(name) == true,
            rawSchema = raw,
        )
    }

    private fun parseType(element: JsonObject): WidgetSchemaTypeDescription {
        val ref = element["\$ref"]?.jsonPrimitive?.content
        if (ref != null) return WidgetSchemaTypeDescription.REFERENCE

        val typeElement = element["type"]
        when (typeElement) {
            is JsonPrimitive -> {
                if (typeElement.isString) {
                    return parseSimpleType(typeElement.content)
                }
            }
            is JsonArray -> {
                val types =
                    typeElement.mapNotNull { primitive ->
                        if (primitive is JsonPrimitive && primitive.isString) primitive.content else null
                    }
                if (types.isNotEmpty()) {
                    return WidgetSchemaTypeDescription.UNION.takeIf { types.size > 1 }
                        ?: parseSimpleType(types.first())
                }
            }
            else -> return WidgetSchemaTypeDescription.UNKNOWN
        }

        element["anyOf"]?.let { anyOf ->
            if (anyOf is JsonArray) return WidgetSchemaTypeDescription.UNION
        }

        if (element["enum"] is JsonArray) return WidgetSchemaTypeDescription.ENUM

        if ("items" in element) return WidgetSchemaTypeDescription.ARRAY
        if ("properties" in element) return WidgetSchemaTypeDescription.OBJECT
        if ("additionalProperties" in element) return WidgetSchemaTypeDescription.MAP

        return WidgetSchemaTypeDescription.UNKNOWN
    }

    private fun JsonElement.asStringSet(): Set<String> =
        (this as? JsonArray)?.mapNotNull { primitive ->
            if (primitive is JsonPrimitive && primitive.isString) primitive.content else null
        }
            ?.toSet()
            .orEmpty()

    private fun parseSimpleType(value: String): WidgetSchemaTypeDescription =
        when (value) {
            "string" -> WidgetSchemaTypeDescription.STRING
            "integer" -> WidgetSchemaTypeDescription.INTEGER
            "number" -> WidgetSchemaTypeDescription.NUMBER
            "boolean" -> WidgetSchemaTypeDescription.BOOLEAN
            "array" -> WidgetSchemaTypeDescription.ARRAY
            "object" -> WidgetSchemaTypeDescription.OBJECT
            "null" -> WidgetSchemaTypeDescription.NULL
            else -> WidgetSchemaTypeDescription.UNKNOWN
        }
}
