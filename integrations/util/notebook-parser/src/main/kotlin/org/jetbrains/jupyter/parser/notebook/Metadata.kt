package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Notebook root-level metadata.
 */
@Serializable
public data class Metadata(
    /** Kernel information. */
    @SerialName("kernelspec")
    val kernelSpec: KernelSpec? = null,
    /** Kernel information. */
    @SerialName("language_info")
    val languageInfo: LanguageInfo? = null,
    /** Original notebook format (major number) before converting the notebook between versions. This should never be written to a file. */
    @SerialName("orig_nbformat")
    val originalNbFormat: Long? = null,
    /** The title of the notebook document */
    val title: String? = null,
    /** The author(s) of the notebook document */
    val authors: List<Author>? = null
) {

    init {
        if (originalNbFormat != null)
            require(originalNbFormat >= 1L) { "orig_nbformat < minimum 1 - $originalNbFormat" }
    }
}
