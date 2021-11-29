package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement

inline fun <reified T> JsonElement.decode(format: Json) = format.decodeFromJsonElement<T>(this)

fun JsonElement?.decodeMultilineText(format: Json): String {
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

val emptyJsonObject = buildJsonObject {}

fun JsonObject?.orEmptyObject() = this ?: emptyJsonObject
