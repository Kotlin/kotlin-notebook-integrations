package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.json.JsonObject

abstract class CellMetadata {
    abstract val name: String?
    abstract val tags: Set<String>?
    abstract val jupyter: JsonObject?

    protected fun validate() {
        name?.let {
            require(CELL_METADATA_NAME_REGEX.containsMatchIn(it)) { "name does not match pattern $CELL_METADATA_NAME_REGEX - $it" }
        }

        tags?.let {
            for (tag in it)
                require(CELL_METADATA_TAGS_REGEX.containsMatchIn(tag)) { "tags item does not match pattern $CELL_METADATA_TAGS_REGEX - $tag" }
        }
    }

    companion object {
        private val CELL_METADATA_NAME_REGEX = Regex("^.+\$")
        private val CELL_METADATA_TAGS_REGEX = Regex("^[^,]+\$")
    }
}
