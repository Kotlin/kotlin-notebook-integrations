package org.jetbrains.kotlinx.jupyter.widget.generation

import kotlinx.serialization.Serializable

@Serializable
public data class WidgetModelDescription(
    val name: String,
    val properties: List<WidgetPropertyDescription>,
)

@Serializable
public data class WidgetPropertyDescription(
    val name: String,
    val type: WidgetSchemaTypeDescription,
    val required: Boolean,
    val rawSchema: String,
)

@Serializable
public enum class WidgetSchemaTypeDescription {
    STRING,
    INTEGER,
    NUMBER,
    BOOLEAN,
    ARRAY,
    OBJECT,
    MAP,
    NULL,
    ENUM,
    REFERENCE,
    UNION,
    UNKNOWN,
}
