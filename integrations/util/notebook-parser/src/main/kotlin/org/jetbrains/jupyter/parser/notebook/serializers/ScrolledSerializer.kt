package org.jetbrains.jupyter.parser.notebook.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.serializer
import org.jetbrains.jupyter.parser.notebook.Scrolled

public object ScrolledSerializer : KSerializer<Scrolled> {
    override val descriptor: SerialDescriptor get() = serializer<JsonPrimitive>().descriptor

    override fun deserialize(decoder: Decoder): Scrolled {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()

        fun scrolledError(): Nothing = throw SerializationException("'scrolled' should be true, false or \"auto\", but was $element")

        if (element !is JsonPrimitive) scrolledError()
        return if (element.isString) {
            if (element.contentOrNull != "auto") scrolledError()
            Scrolled.AUTOSCROLLED
        } else {
            val flag = element.decode<Boolean>(decoder.json)
            if (flag) Scrolled.SCROLLED else Scrolled.UNSCROLLED
        }
    }

    override fun serialize(encoder: Encoder, value: Scrolled) {
        require(encoder is JsonEncoder)
        val json = when (value) {
            Scrolled.SCROLLED -> JsonPrimitive(true)
            Scrolled.UNSCROLLED -> JsonPrimitive(false)
            Scrolled.AUTOSCROLLED -> JsonPrimitive("auto")
        }
        encoder.encodeJsonElement(json)
    }
}
