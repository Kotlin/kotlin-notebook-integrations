package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.json.JsonObject

class MarkdownCell(
    override val id: String?,
    override val metadata: MarkdownCellMetadata,
    override val source: String,
    val attachments: JsonObject? = null,
) : Cell() {
    override val type: Type get() = Type.MARKDOWN
}
