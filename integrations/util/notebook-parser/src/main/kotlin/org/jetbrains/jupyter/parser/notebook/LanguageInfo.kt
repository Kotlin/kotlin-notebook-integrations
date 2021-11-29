package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Kernel information.
 */
@Serializable
data class LanguageInfo(
    /** The programming language which this kernel runs. */
    val name: String,
    /** The codemirror mode to use for code in this language. */
    @SerialName("codemirror_mode")
    val codemirrorMode: JsonElement? = null,
    /** The file extension for files in this language. */
    @SerialName("file_extension")
    val fileExtension: String? = null,
    /** The mimetype corresponding to files in this language. */
    val mimetype: String? = null,
    /** The pygments lexer to use for code in this language. */
    @SerialName("pygments_lexer")
    val pygmentsLexer: String? = null
)
