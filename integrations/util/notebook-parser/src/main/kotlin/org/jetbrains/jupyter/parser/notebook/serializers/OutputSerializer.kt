package org.jetbrains.jupyter.parser.notebook.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import org.jetbrains.jupyter.parser.notebook.DisplayData
import org.jetbrains.jupyter.parser.notebook.Error
import org.jetbrains.jupyter.parser.notebook.ExecuteResult
import org.jetbrains.jupyter.parser.notebook.Output
import org.jetbrains.jupyter.parser.notebook.Stream
import org.jetbrains.jupyter.parser.notebook.decode
import org.jetbrains.jupyter.parser.notebook.decodeMultilineText
import org.jetbrains.jupyter.parser.notebook.orEmptyObject

object OutputSerializer : KSerializer<Output> {
    override val descriptor: SerialDescriptor = serializer<JsonObject>().descriptor

    override fun deserialize(decoder: Decoder): Output {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement().jsonObject
        val format = decoder.json

        return when (val outputTypeString = element["output_type"]?.decode<String>(format)) {
            "execute_result" -> {
                val data = element["data"]?.decode<JsonObject>(format).orEmptyObject()
                val metadata = element["metadata"]?.decode<JsonObject>(format).orEmptyObject()
                val executionCount = element["execution_count"]?.decode<Long>(format)
                ExecuteResult(data, metadata, executionCount)
            }
            "display_data" -> {
                val data = element["data"]?.decode<JsonObject>(format).orEmptyObject()
                val metadata = element["metadata"]?.decode<JsonObject>(format).orEmptyObject()
                DisplayData(data, metadata)
            }
            "stream" -> {
                val name = element["name"]?.decode<String>(format).orEmpty()
                val text = element["text"].decodeMultilineText(format)
                Stream(name, text)
            }
            "error" -> {
                val ename = element["ename"]?.decode<String>(format).orEmpty()
                val evalue = element["evalue"]?.decode<String>(format).orEmpty()
                val traceback = element["traceback"]?.decode<List<String>>(format).orEmpty()
                Error(ename, evalue, traceback)
            }
            else -> {
                throw SerializationException("Unknown output type: $outputTypeString")
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Output) {
    }
}
