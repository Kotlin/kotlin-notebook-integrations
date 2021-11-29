package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.json.JsonObject

sealed class CellWithAttachments : Cell() {
    abstract val attachments: JsonObject?
}
