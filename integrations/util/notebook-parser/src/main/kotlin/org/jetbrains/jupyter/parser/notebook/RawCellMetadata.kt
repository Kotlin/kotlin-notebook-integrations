package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
public class RawCellMetadata(
    override val name: String? = null,
    override val tags: Set<String>? = null,
    override val jupyter: JsonObject? = null,
    public val format: String? = null,
) : CellMetadata()
