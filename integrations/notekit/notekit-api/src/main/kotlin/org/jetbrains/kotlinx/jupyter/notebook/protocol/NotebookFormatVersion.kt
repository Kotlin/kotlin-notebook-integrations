package org.jetbrains.kotlinx.jupyter.notebook.protocol

/**
 * Represents the notebook format version.
 */
public data class NotebookFormatVersion(
    val major: Int,
    val minor: Long,
)
