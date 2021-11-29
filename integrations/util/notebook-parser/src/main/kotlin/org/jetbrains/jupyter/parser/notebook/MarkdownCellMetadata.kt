package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Cell-level metadata.
 */
@Serializable
class MarkdownCellMetadata(
    override val name: String? = null,
    override val tags: Set<String>? = null,
    override val jupyter: JsonObject? = null,
) : CellMetadata()
