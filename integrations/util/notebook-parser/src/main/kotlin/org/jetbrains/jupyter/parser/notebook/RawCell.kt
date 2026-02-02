package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.json.JsonObject

public class RawCell(
    override val id: String?,
    override val metadata: RawCellMetadata,
    override val source: String,
    override val attachments: JsonObject? = null,
) : CellWithAttachments() {
    override val type: Type get() = Type.RAW
}
