package org.jetbrains.jupyter.parser.notebook.serializers

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

internal inline fun <reified T> JsonElement.decode(format: Json) = format.decodeFromJsonElement<T>(this)

internal fun JsonElement?.decodeMultilineText(format: Json): String {
    return when (this) {
        is JsonArray -> {
            val lines = decode<List<String>>(format)
            lines.joinToString("")
        }
        is JsonPrimitive -> {
            decode(format)
        }
        else -> ""
    }
}

internal fun Json.encodeMultilineText(text: String): JsonElement {
    return encodeToJsonElement(text.splitNoTrim('\n'))
}

private fun String.splitNoTrim(delimiter: Char): List<String> {
    var currentOffset = 0
    var nextIndex = indexOf(delimiter, currentOffset)
    if (nextIndex == -1) {
        return listOf(this)
    }

    return buildList {
        do {
            add(substring(currentOffset, nextIndex + 1))
            currentOffset = nextIndex + 1
            nextIndex = indexOf(delimiter, currentOffset)
        } while (nextIndex != -1)
        add(substring(currentOffset, length))
    }
}

internal val emptyJsonObject = buildJsonObject {}

internal fun JsonObject?.orEmptyObject() = this ?: emptyJsonObject

internal fun JsonElement?.decodeDisplayMap(format: Json): Map<String, String> {
    if (this == null) return emptyMap()

    val jsonObject = decode<JsonObject>(format)
    return buildMap {
        for ((key, value) in jsonObject) {
            val textValue = value.decodeMultilineText(format)
            put(key, textValue)
        }
    }
}

internal fun Json.encodeDisplayMap(map: Map<String, String>): JsonObject {
    return buildJsonObject {
        for ((key, textValue) in map) {
            val value = encodeMultilineText(textValue)
            put(key, value)
        }
    }
}
