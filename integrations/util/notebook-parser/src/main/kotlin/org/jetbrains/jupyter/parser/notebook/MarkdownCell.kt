package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.json.JsonObject

public class MarkdownCell(
    override val id: String?,
    override val metadata: MarkdownCellMetadata,
    override val source: String,
    override val attachments: JsonObject? = null,
) : CellWithAttachments() {
    override val type: Type get() = Type.MARKDOWN
}
