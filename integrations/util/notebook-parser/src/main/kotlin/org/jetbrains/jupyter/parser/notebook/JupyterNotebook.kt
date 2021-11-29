package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JupyterNotebook(
    /** Notebook root-level metadata. */
    val metadata: Metadata,
    /** Notebook format (minor number). Incremented for backward compatible changes to the notebook format. */
    @SerialName("nbformat_minor")
    val nbformatMinor: Long,
    /** Notebook format (major number). Incremented between backwards incompatible changes to the notebook format. */
    val nbformat: Int,
    /** Array of cells of the current notebook. */
    val cells: List<Cell>
) {

    init {
        require(nbformatMinor >= 0L) { "nbformatMinor < minimum 0 - $nbformatMinor" }
        require(nbformat in 4..4) { "nbformat not in range 4..4 - $nbformat" }
    }
}
