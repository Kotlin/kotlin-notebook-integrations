package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.json.JsonObject

public sealed class CellWithAttachments : Cell() {
    public abstract val attachments: JsonObject?
}
