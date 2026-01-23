package org.jetbrains.kotlinx.jupyter.widget.generator

/**
 * Information about a widget trait (shared properties or base class).
 */
internal data class TraitInfo(
    val baseClassName: String,
    val traitProperties: Map<String, String?>,
    val import: String? = null,
    val allowedClasses: Set<String>? = null,
    val isInterface: Boolean = false,
    val shouldOverride: Boolean = false,
    val skipGeneration: Boolean = false,
    val extends: String? = null,
)

internal data class OptionWidgetTraitInfo(
    val baseClassName: String,
    val indexType: String,
)

private fun OptionWidgetTraitInfo.toTraitInfo(): TraitInfo =
    TraitInfo(
        baseClassName = baseClassName,
        traitProperties =
            mapOf(
                "_options_labels" to "List<String>",
                "index" to indexType,
            ),
        import = "$WIDGET_LIBRARY_PACKAGE.options.$baseClassName",
        isInterface = false,
        shouldOverride = false,
        skipGeneration = true,
        extends = "DomWidgetBase",
    )

internal val traits: List<TraitInfo> =
    listOf(
        TraitInfo(
            baseClassName = "MediaWidget",
            traitProperties =
                mapOf(
                    "value" to "ByteArray",
                    "format" to "String",
                ),
            import = "$WIDGET_LIBRARY_PACKAGE.media.MediaWidget",
            allowedClasses = setOf("AudioWidget", "ImageWidget", "VideoWidget"),
            isInterface = true,
            shouldOverride = true,
            skipGeneration = false,
        ),
        TraitInfo(
            baseClassName = "DomWidgetBase",
            traitProperties =
                mapOf(
                    "_dom_classes" to "List<String>",
                    "layout" to "LayoutWidget?",
                    "tabbable" to "Boolean?",
                    "tooltip" to "String?",
                ),
            isInterface = false,
            shouldOverride = true,
            skipGeneration = true,
        ),
        TraitInfo(
            baseClassName = "WidgetWithDescription",
            traitProperties =
                mapOf(
                    "description" to "String",
                ),
            isInterface = true,
            shouldOverride = true,
            skipGeneration = false,
        ),
        TraitInfo(
            baseClassName = "ContainerWidget",
            traitProperties =
                mapOf(
                    "children" to "List<WidgetModel?>",
                ),
            isInterface = true,
            shouldOverride = true,
            skipGeneration = false,
        ),
        TraitInfo(
            baseClassName = "LinkWidgetBase",
            traitProperties =
                mapOf(
                    "source" to "Pair<WidgetModel?, String>?",
                    "target" to "Pair<WidgetModel?, String>?",
                ),
            import = "$WIDGET_LIBRARY_PACKAGE.links.LinkWidgetBase",
            isInterface = false,
            shouldOverride = true,
            skipGeneration = true,
        ),
    ) +
        listOf(
            OptionWidgetTraitInfo(
                baseClassName = "SingleNullableSelectionWidgetBase",
                indexType = "Int?",
            ),
            OptionWidgetTraitInfo(
                baseClassName = "SingleSelectionWidgetBase",
                indexType = "Int",
            ),
            OptionWidgetTraitInfo(
                baseClassName = "MultipleSelectionWidgetBase",
                indexType = "List<Int>",
            ),
            OptionWidgetTraitInfo(
                baseClassName = "SelectionRangeWidgetBase",
                indexType = "IntRange?",
            ),
        ).map { it.toTraitInfo() }

/**
 * Finds all traits that match the given widget class and its attributes.
 */
internal fun findMatchedTraits(
    className: String,
    attributeProperties: Map<String, PropertyType>,
): List<TraitInfo> {
    return traits.filter { trait ->
        val classMatches = trait.allowedClasses == null || className in trait.allowedClasses
        classMatches &&
            trait.traitProperties.all { (name, type) ->
                val attrType = attributeProperties[name] ?: return@all false
                type == null || attrType.kotlinType == type
            }
    }
}

/**
 * Finds the most specific base class trait among the matched traits.
 * Throws an error if multiple most specific base class traits are found.
 */
internal fun findBaseClassTrait(
    matchedTraits: List<TraitInfo>,
    className: String,
): TraitInfo? {
    val baseClassTraits = matchedTraits.filter { !it.isInterface }
    if (baseClassTraits.isEmpty()) return null

    val traitByName = traits.associateBy { it.baseClassName }

    fun isSubTrait(
        sub: TraitInfo,
        sup: TraitInfo,
    ): Boolean {
        if (sub == sup) return false
        var current: TraitInfo? = sub
        while (true) {
            current = current?.extends?.let { traitByName[it] }
            if (current == null) return false
            if (current == sup) return true
        }
    }

    val mostSpecific =
        baseClassTraits.filter { trait ->
            baseClassTraits.none { other -> isSubTrait(other, trait) }
        }

    if (mostSpecific.size > 1) {
        error(
            "Widget $className matches multiple most specific base class traits: " +
                mostSpecific.map { it.baseClassName },
        )
    }
    return mostSpecific.first()
}
