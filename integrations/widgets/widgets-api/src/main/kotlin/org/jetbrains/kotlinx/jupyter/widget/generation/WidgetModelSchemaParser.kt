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
        val spec = parseSpec(properties) ?: return null
        val parsedProperties =
            properties.mapNotNull { (propertyName, propertyElement) ->
                parseProperty(propertyName, propertyElement, required)
            }
        return WidgetModelDescription(spec.controlName, spec.description, parsedProperties)
    }

    private fun parseProperty(
        name: String,
        element: JsonElement,
        required: Set<String>?,
    ): WidgetPropertyDescription? {
        if (name.startsWith("_")) return null

        val objectElement = element.jsonObject
        val description = parseType(objectElement)
        val ref = objectElement["\$ref"]?.jsonPrimitive?.content
        val default = objectElement["default"]
        return WidgetPropertyDescription(
            name = name,
            type = description,
            required = required?.contains(name) == true,
            ref = ref,
            defaultValue = default,
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

    private fun parseSpec(properties: JsonObject): WidgetSpecComponents? {
        val modelName = properties["_model_name"].asStringDefault()
        val modelModule = properties["_model_module"].asStringDefault()
        val modelModuleVersion = properties["_model_module_version"].asStringDefault()
        val viewName = properties["_view_name"].asStringDefault()
        val viewModule = properties["_view_module"].asStringDefault()
        val viewModuleVersion = properties["_view_module_version"].asStringDefault()

        if (
            modelName == null ||
            modelModule == null ||
            modelModuleVersion == null ||
            viewName == null ||
            viewModule == null ||
            viewModuleVersion == null
        ) return null

        val controlName = modelName.removeSuffix("Model")
        return WidgetSpecComponents(
            controlName = controlName,
            description = WidgetSpecDescription(
                modelName = modelName,
                modelModule = modelModule,
                modelModuleVersion = modelModuleVersion,
                viewName = viewName,
                viewModule = viewModule,
                viewModuleVersion = viewModuleVersion,
            ),
        )
    }

    private fun JsonElement?.asStringDefault(): String? {
        val element = this ?: return null
        val obj = element as? JsonObject ?: return null
        val defaultValue = obj["default"]
        if (defaultValue is JsonPrimitive && defaultValue.isString) return defaultValue.content
        val enumValues = obj["enum"] as? JsonArray
        val firstEnum = enumValues?.firstOrNull() as? JsonPrimitive
        if (firstEnum != null && firstEnum.isString) return firstEnum.content
        val constValue = obj["const"] as? JsonPrimitive
        if (constValue != null && constValue.isString) return constValue.content
        return null
    }

    private data class WidgetSpecComponents(
        val controlName: String,
        val description: WidgetSpecDescription,
    )

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
