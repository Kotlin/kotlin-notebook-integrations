package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.Serializable
import org.jetbrains.jupyter.parser.notebook.serializers.CellSerializer

@Serializable(CellSerializer::class)
sealed class Cell {
    abstract val id: String?
    abstract val metadata: CellMetadata
    abstract val source: String
    abstract val type: Type

    init {
        id?.let {
            require(it.length in 1..64) { "id length not in range 1..64 - ${it.length}" }
            require(CELL_ID_REGEX.containsMatchIn(it)) { "id does not match pattern $CELL_ID_REGEX - $it" }
        }
    }

    enum class Type {
        CODE,
        MARKDOWN,
        RAW,
    }

    companion object {
        private val CELL_ID_REGEX = Regex("^[a-zA-Z0-9-_]+\$")
    }
}
