package org.jetbrains.kotlinx.jupyter.widget.generator

/**
 * Custom overrides for widget properties that shouldn't be in schema.json.
 */
internal data class PropertyOverride(
    val name: String? = null,
    val visibility: Visibility? = null,
) {
    fun applyTo(attribute: AttributeSchema): AttributeSchema =
        attribute.copy(
            kotlinName = name ?: attribute.kotlinName,
            visibility = visibility ?: attribute.visibility,
        )
}

/**
 * Applies overrides for the given [className] and [attribute] if they exist.
 */
internal fun applyPropertyOverrides(
    className: String,
    attribute: AttributeSchema,
): AttributeSchema {
    val classOverrides = propertyOverrides[className] ?: return attribute
    val override = classOverrides[attribute.name] ?: return attribute
    return override.applyTo(attribute)
}

/**
 * A map from widget class name to a map of attribute names and their overrides.
 */
internal val propertyOverrides: Map<String, Map<String, PropertyOverride>> =
    mapOf(
        "OutputWidgetBase" to
            mapOf(
                "msg_id" to PropertyOverride(visibility = Visibility.INTERNAL),
                "outputs" to PropertyOverride(name = "_outputs", visibility = Visibility.INTERNAL),
            ),
    )
