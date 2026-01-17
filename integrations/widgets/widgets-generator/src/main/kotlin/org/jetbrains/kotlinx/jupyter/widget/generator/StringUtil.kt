package org.jetbrains.kotlinx.jupyter.widget.generator

private val commonAbbreviations: Set<String> =
    setOf(
        "html",
        "dom",
    )

/**
 * Converts a string to camelCase, taking into account common abbreviations.
 */
internal fun String.toCamelCase(): String {
    if (isEmpty()) return this
    if (length == 1) return lowercase()
    val abbreviation = commonAbbreviations.find { startsWith(it, ignoreCase = true) }
    val suffixStartIndex =
        if (abbreviation == null) {
            var index = 1
            while (index < length && this[index].isUpperCase()) {
                index++
            }
            index
        } else {
            abbreviation.length
        }

    val prefix = substring(0, suffixStartIndex)
    val rest = substring(suffixStartIndex)
    return prefix.lowercase() + rest
}

/**
 * Converts a snake_case, kebab-case, or space-separated string to PascalCase.
 */
internal fun String.toPascalCase(): String =
    split('_', '-', ' ').joinToString("") { part ->
        part.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
