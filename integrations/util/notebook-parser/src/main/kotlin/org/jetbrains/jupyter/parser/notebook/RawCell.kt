package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.json.JsonObject

class RawCell(
    override val id: String?,
    override val metadata: RawCellMetadata,
    override val source: String,
    val attachments: JsonObject? = null,
) : Cell() {
    override val type: Type get() = Type.RAW
}
