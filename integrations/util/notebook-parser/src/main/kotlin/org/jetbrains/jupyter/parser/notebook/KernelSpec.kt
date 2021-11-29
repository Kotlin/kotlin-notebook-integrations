package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Kernel information.
 */
@Serializable
data class KernelSpec(
    /** Name of the kernel specification. */
    val name: String,
    /** Name to display in UI. */
    @SerialName("display_name")
    val displayName: String
)
