package org.jetbrains.kotlinx.jupyter.widget.generator

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Visibility modifier for a widget attribute.
 */
internal enum class Visibility {
    PUBLIC,
    INTERNAL,
    PRIVATE,
    PROTECTED,
    ;

    override fun toString(): String = name.lowercase()
}

/**
 * Represents the schema for a single widget attribute in `schema.json`.
 */
@Serializable
internal data class AttributeSchema(
    val name: String,
    val type: AttributeType,
    val default: JsonElement,
    @SerialName("allow_none") val allowNone: Boolean = false,
    val enum: List<String> = emptyList(),
    val help: String = "",
    val items: AttributeItems? = null,
    val widget: String? = null,
    @SerialName("union_attributes") val unionAttributes: List<JsonObject> = emptyList(),
    val visibility: Visibility = Visibility.PUBLIC,
    val kotlinName: String = name.toCamelCase(),
)

@Serializable
internal data class AttributeItems(
    val type: AttributeType,
    val widget: String? = null,
)

private object AttributeTypeSerializer {
    fun serialize(type: AttributeType): JsonElement =
        when (type) {
            is AttributeType.Single -> JsonPrimitive(type.name)
            is AttributeType.Union -> Json.encodeToJsonElement(type.options.map { serialize(it) })
        }
}

@Serializable(with = AttributeType.Companion::class)
internal sealed class AttributeType {
    data class Single(
        val name: String,
    ) : AttributeType()

    data class Union(
        val options: List<Single>,
    ) : AttributeType()

    companion object : KSerializer<AttributeType> {
        private val delegate = JsonElement.serializer()

        override val descriptor = delegate.descriptor

        override fun deserialize(decoder: Decoder): AttributeType =
            when (val element = delegate.deserialize(decoder)) {
                is JsonPrimitive -> Single(element.content)
                else -> {
                    val options = Json.decodeFromJsonElement<List<JsonPrimitive>>(element)
                    Union(options.map { Single(it.content) })
                }
            }

        override fun serialize(
            encoder: Encoder,
            value: AttributeType,
        ) {
            encoder.encodeSerializableValue(delegate, AttributeTypeSerializer.serialize(value))
        }
    }
}
