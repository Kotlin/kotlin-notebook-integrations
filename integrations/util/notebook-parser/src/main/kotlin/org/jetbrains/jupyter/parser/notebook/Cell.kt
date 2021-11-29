package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.Serializable
import org.jetbrains.jupyter.parser.notebook.serializers.CellSerializer

@Serializable(CellSerializer::class)
public sealed class Cell {
    public abstract val id: String?
    public abstract val metadata: CellMetadata
    public abstract val source: String
    public abstract val type: Type

    init {
        verify()
    }

    private fun verify() {
        id?.let {
            require(it.length in 1..64) { "id length not in range 1..64 - ${it.length}" }
            require(CELL_ID_REGEX.containsMatchIn(it)) { "id does not match pattern $CELL_ID_REGEX - $it" }
        }
    }

    public enum class Type {
        CODE,
        MARKDOWN,
        RAW,
    }

    private companion object {
        private val CELL_ID_REGEX = Regex("^[a-zA-Z0-9-_]+\$")
    }
}
