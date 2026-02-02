package org.jetbrains.kotlinx.jupyter.notebook

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Converts a Map with String keys and arbitrary values to a JsonElement.
 * Supports nested maps, lists, primitives (String, Number, Boolean), and null.
 * Unsupported types are converted to strings via toString().
 */
internal fun mapToJson(map: Map<String, Any?>): JsonElement =
    buildJsonObject {
        map.forEach { (key, value) ->
            put(key, anyToJson(value))
        }
    }

/**
 * Converts a List with arbitrary values to a JsonElement (JsonArray).
 * Supports nested maps, lists, primitives (String, Number, Boolean), and null.
 * Unsupported types are converted to strings via toString().
 */
internal fun listToJson(list: List<*>): JsonElement =
    buildJsonArray {
        list.forEach { value ->
            add(anyToJson(value))
        }
    }

/**
 * Converts any Kotlin value to a JsonElement.
 * Supports maps, lists, primitives (String, Number, Boolean), and null.
 * Unsupported types are converted to strings via toString().
 */
internal fun anyToJson(value: Any?): JsonElement =
    when (value) {
        null -> JsonNull
        is String -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is Map<*, *> -> {
            @Suppress("UNCHECKED_CAST")
            mapToJson(value as Map<String, Any?>)
        }
        is List<*> -> listToJson(value)
        else -> JsonPrimitive(value.toString())
    }

/**
 * Converts a JsonObject to a Map with String keys and arbitrary values.
 */
internal fun jsonToMap(json: JsonObject): Map<String, Any?> =
    json.mapValues { (_, value) ->
        jsonToAny(value)
    }

/**
 * Converts a JsonElement to a Kotlin value (Map, List, String, Number, Boolean, or null).
 * - JsonObject -> Map<String, Any?>
 * - JsonArray -> List<Any?>
 * - JsonNull -> null
 * - JsonPrimitive -> String, Boolean, Long, Double, or String (fallback)
 */
internal fun jsonToAny(element: JsonElement): Any? =
    when {
        element is JsonNull -> null
        element is JsonObject -> jsonToMap(element)
        element is JsonArray -> element.map { jsonToAny(it) }
        element.jsonPrimitive.isString -> element.jsonPrimitive.content
        else -> {
            val primitive = element.jsonPrimitive
            primitive.content.toBooleanStrictOrNull()
                ?: primitive.content.toLongOrNull()
                ?: primitive.content.toDoubleOrNull()
                ?: primitive.content
        }
    }
