package org.jetbrains.kotlinx.jupyter.widget.generator

private val commonAbbreviations: Set<String> =
    setOf(
        "html",
        "dom",
    )

/**
 * Ensures that a widget name ends with "Widget" and converts it to PascalCase.
 */
internal fun String.toWidgetClassName(): String = (if (endsWith("Widget")) this else "${this}Widget").toPascalCase()

/**
 * Converts a string to PascalCase, taking into account common abbreviations and delimiters.
 */
internal fun String.toPascalCase(): String {
    val parts = splitIntoParts()
    if (parts.isEmpty()) return this

    return parts.joinToString("") { part ->
        part.toPascalCasePart()
    }
}

/**
 * Converts a string to camelCase, taking into account common abbreviations and delimiters.
 */
internal fun String.toCamelCase(): String {
    val parts = splitIntoParts()
    if (parts.isEmpty()) return this

    return buildString {
        append(parts[0].lowercase())
        for (i in 1 until parts.size) {
            append(parts[i].toPascalCasePart())
        }
    }
}

/**
 * Splits a string into parts based on common delimiters (undescore, hyphen, space)
 * or by detecting transitions between lowercase and uppercase letters (PascalCase/camelCase).
 * Abbreviations are treated as separate parts.
 */
private fun String.splitIntoParts(): List<String> {
    if (isEmpty()) return emptyList()

    val parts = mutableListOf<String>()
    val currentPart = StringBuilder()

    var i = 0
    while (i < length) {
        val c = this[i]
        if (c == '_' || c == '-' || c == ' ') {
            if (currentPart.isNotEmpty()) {
                parts.add(currentPart.toString())
                currentPart.clear()
            }
            i++
            continue
        }

        // Check for common abbreviations
        val abbreviation = commonAbbreviations.find { regionMatches(i, it, 0, it.length, ignoreCase = true) }
        if (abbreviation != null) {
            if (currentPart.isNotEmpty()) {
                parts.add(currentPart.toString())
                currentPart.clear()
            }
            parts.add(abbreviation)
            i += abbreviation.length
            continue
        }

        // Detect PascalCase/camelCase transitions
        if (c.isUpperCase() && currentPart.isNotEmpty()) {
            val last = currentPart.last()
            if (!last.isUpperCase() || (i + 1 < length && this[i + 1].isLowerCase())) {
                parts.add(currentPart.toString())
                currentPart.clear()
            }
        }

        currentPart.append(c)
        i++
    }
    if (currentPart.isNotEmpty()) {
        parts.add(currentPart.toString())
    }
    return parts
}

private fun String.toPascalCasePart(): String = lowercase().replaceFirstChar { it.uppercase() }
