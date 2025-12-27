package org.jetbrains.kotlinx.jupyter.widget.model

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

public typealias CustomMessageListener = (
    content: JsonObject,
    metadata: JsonElement?,
    buffers: List<ByteArray>,
) -> Unit
