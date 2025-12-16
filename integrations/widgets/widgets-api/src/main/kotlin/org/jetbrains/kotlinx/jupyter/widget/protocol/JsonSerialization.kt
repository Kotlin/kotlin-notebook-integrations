package org.jetbrains.kotlinx.jupyter.widget.protocol

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlin.collections.iterator

internal fun serializeJsonMap(map: Patch): JsonElement = serialize(map)

private fun serializeAny(obj: Any?): JsonElement =
    when (obj) {
        null -> JsonNull
        is Map<*, *> -> serialize(obj)
        is List<*> -> serialize(obj)
        is String -> JsonPrimitive(obj)
        is Boolean -> JsonPrimitive(obj)
        is Number -> JsonPrimitive(obj)
        is ByteArray -> JsonNull
        else -> error("Don't know how to serialize object [$obj] of class ${obj::class}")
    }

private fun serialize(map: Map<*, *>): JsonObject =
    buildJsonObject {
        for ((key, value) in map) {
            if (key !is String) error("Map key [$key] is of type ${key?.let { it::class }}. Don't know how to serialize it.")
            put(key, serializeAny(value))
        }
    }

private fun serialize(list: List<*>): JsonArray =
    buildJsonArray {
        for (value in list) {
            add(serializeAny(value))
        }
    }

internal fun deserializeJsonMap(json: JsonElement): MutableMap<String, Any?> {
    if (json !is JsonObject) error("Input json should be a key-value object, but it's $json")
    return deserializeMap(json)
}

private fun deserializeAny(json: JsonElement): Any? =
    when (json) {
        is JsonObject -> deserializeMap(json)
        is JsonArray -> deserializeList(json)
        is JsonPrimitive -> deserializePrimitive(json)
    }

private fun deserializePrimitive(json: JsonPrimitive): Any? =
    when {
        json is JsonNull -> null
        json.isString -> json.content
        else -> {
            json.booleanOrNull ?: json.doubleOrNull ?: error("Unknown JSON primitive type: [$json]")
        }
    }

private fun deserializeMap(json: JsonObject): MutableMap<String, Any?> =
    mutableMapOf<String, Any?>().apply {
        for ((key, value) in json) {
            put(key, deserializeAny(value))
        }
    }

private fun deserializeList(jsonArray: JsonArray): MutableList<Any?> =
    mutableListOf<Any?>().apply {
        for (el in jsonArray) {
            add(deserializeAny(el))
        }
    }
