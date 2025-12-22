package org.jetbrains.kotlinx.jupyter.widget.generation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class WidgetModelDescription(
    val controlName: String,
    val spec: WidgetSpecDescription,
    val properties: List<WidgetPropertyDescription>,
)

@Serializable
public data class WidgetSpecDescription(
    val modelName: String,
    val modelModule: String,
    val modelModuleVersion: String,
    val viewName: String,
    val viewModule: String,
    val viewModuleVersion: String,
)

@Serializable
public data class WidgetPropertyDescription(
    val name: String,
    val type: WidgetSchemaTypeDescription,
    val required: Boolean,
    val ref: String? = null,
    val defaultValue: JsonElement? = null,
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
