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
import kotlinx.serialization.json.intOrNull
import kotlin.collections.iterator

/**
 * Converts a [Patch] (Map<String, WidgetValue>) to its JSON representation.
 * Note: [RawPropertyValue.ByteArrayValue] values are serialized as [JsonNull]
 * because they should be handled via binary buffers.
 */
internal fun serializeJsonMap(map: Patch): JsonElement = serialize(map)

private fun serializeAny(obj: RawPropertyValue): JsonElement =
    when (obj) {
        is RawPropertyValue.Null -> JsonNull
        is RawPropertyValue.MapValue -> serialize(obj.values)
        is RawPropertyValue.ListValue -> serialize(obj)
        is RawPropertyValue.StringValue -> JsonPrimitive(obj.value)
        is RawPropertyValue.BooleanValue -> JsonPrimitive(obj.value)
        is RawPropertyValue.NumberValue -> JsonPrimitive(obj.value)
        is RawPropertyValue.ByteArrayValue -> JsonNull // Binary data is sent separately in the 'buffers' field
    }

private fun serialize(map: Map<String, RawPropertyValue>): JsonObject =
    buildJsonObject {
        for ((key, value) in map) {
            put(key, serializeAny(value))
        }
    }

private fun serialize(listValue: RawPropertyValue.ListValue): JsonArray =
    buildJsonArray {
        for (value in listValue.values) {
            add(serializeAny(value))
        }
    }

/**
 * Converts a JSON element back to a mutable map of property values.
 */
internal fun deserializeJsonMap(json: JsonElement): RawPropertyValue.MapValue {
    if (json !is JsonObject) error("Input json should be a key-value object, but it's $json")
    return deserializeMap(json)
}

private fun deserializeAny(json: JsonElement): RawPropertyValue =
    when (json) {
        is JsonObject -> deserializeMap(json)
        is JsonArray -> deserializeList(json)
        is JsonPrimitive -> deserializePrimitive(json)
    }

private fun deserializePrimitive(json: JsonPrimitive): RawPropertyValue =
    when {
        json is JsonNull -> RawPropertyValue.Null
        json.isString -> RawPropertyValue.StringValue(json.content)
        else -> {
            json.booleanOrNull?.let { RawPropertyValue.BooleanValue(it) }
                ?: json.intOrNull?.let { RawPropertyValue.NumberValue(it) }
                ?: json.doubleOrNull?.let { RawPropertyValue.NumberValue(it) }
                ?: error("Unknown JSON primitive type: [$json]")
        }
    }

private fun deserializeMap(json: JsonObject): RawPropertyValue.MapValue =
    RawPropertyValue.MapValue(
        mutableMapOf<String, RawPropertyValue>().apply {
            for ((key, value) in json) {
                put(key, deserializeAny(value))
            }
        },
    )

private fun deserializeList(jsonArray: JsonArray): RawPropertyValue.ListValue =
    RawPropertyValue.ListValue(
        mutableListOf<RawPropertyValue>().apply {
            for (el in jsonArray) {
                add(deserializeAny(el))
            }
        },
    )
