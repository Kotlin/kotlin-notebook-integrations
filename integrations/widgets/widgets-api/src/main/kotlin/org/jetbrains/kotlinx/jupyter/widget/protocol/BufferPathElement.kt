package org.jetbrains.kotlinx.jupyter.widget.protocol

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

/**
 * Elements that can appear in a buffer path (either a string key or an integer index).
 */
@Serializable(with = BufferPathElementSerializer::class)
public sealed interface BufferPathElement {
    public data class Key(
        val key: String,
    ) : BufferPathElement

    public data class Index(
        val index: Int,
    ) : BufferPathElement
}

public typealias BufferPath = List<BufferPathElement>

/**
 * Serializer for [BufferPathElement].
 * A [BufferPathElement.Key] is serialized as a JSON string.
 * A [BufferPathElement.Index] is serialized as a JSON integer.
 */
internal object BufferPathElementSerializer : KSerializer<BufferPathElement> {
    override val descriptor: SerialDescriptor = serialDescriptor<JsonElement>()

    override fun serialize(
        encoder: Encoder,
        value: BufferPathElement,
    ) {
        val jsonEncoder =
            encoder as? JsonEncoder
                ?: error("BufferPathElementSerializer can only be used with JSON")

        val element =
            when (value) {
                is BufferPathElement.Key -> JsonPrimitive(value.key)
                is BufferPathElement.Index -> JsonPrimitive(value.index)
            }
        jsonEncoder.encodeJsonElement(element)
    }

    override fun deserialize(decoder: Decoder): BufferPathElement {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: error("BufferPathElementSerializer can only be used with JSON")

        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonPrimitive) {
            if (element.isString) return BufferPathElement.Key(element.content)
            element.intOrNull?.let { return BufferPathElement.Index(it) }
        }
        error("Unsupported buffer path element: $element")
    }
}
