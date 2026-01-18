package org.jetbrains.kotlinx.jupyter.widget.test.util

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

sealed interface CommEvent {
    data class Open(
        val commId: String,
        val targetName: String,
        val data: JsonObject,
        val metadata: JsonElement?,
        val buffers: List<ByteArray>,
    ) : CommEvent

    data class Message(
        val commId: String,
        val data: JsonObject,
        val metadata: JsonElement?,
        val buffers: List<ByteArray>,
    ) : CommEvent

    data class Close(
        val commId: String,
        val data: JsonObject,
        val metadata: JsonElement?,
        val buffers: List<ByteArray>,
    ) : CommEvent
}
