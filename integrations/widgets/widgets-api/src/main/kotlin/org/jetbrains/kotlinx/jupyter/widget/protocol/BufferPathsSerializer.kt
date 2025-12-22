package org.jetbrains.kotlinx.jupyter.widget.protocol

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

internal object BufferPathsSerializer : KSerializer<List<List<Any>>> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        listSerialDescriptor(
            listSerialDescriptor(
                serialDescriptor<JsonElement>(),
            ),
        )

    override fun serialize(
        encoder: Encoder,
        value: List<List<Any>>,
    ) {
        val jsonEncoder =
            encoder as? JsonEncoder
                ?: error("BufferPathsSerializer can only be used with JSON")
        jsonEncoder.encodeJsonElement(
            JsonArray(
                value.map { path ->
                    JsonArray(
                        path.map { el ->
                            when (el) {
                                is String -> JsonPrimitive(el)
                                is Number -> JsonPrimitive(el)
                                else -> error("Unsupported buffer path element: $el (${el::class.simpleName})")
                            }
                        },
                    )
                },
            ),
        )
    }

    override fun deserialize(decoder: Decoder): List<List<Any>> {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: error("BufferPathsSerializer can only be used with JSON")
        val element = jsonDecoder.decodeJsonElement()

        require(element is JsonArray) { "Expected JSON array for buffer_paths, got: $element" }

        return element.map { pathEl ->
            require(pathEl is JsonArray) { "Expected JSON array inside buffer_paths, got: $pathEl" }
            pathEl.map { element ->
                element as? JsonPrimitive ?: error("Expected JSON primitive inside buffer_paths, got: $element")
                if (element.isString) return@map element.content
                element.intOrNull ?: element.doubleOrNull ?: error("Unsupported buffer path element: $element")
            }
        }
    }
}
