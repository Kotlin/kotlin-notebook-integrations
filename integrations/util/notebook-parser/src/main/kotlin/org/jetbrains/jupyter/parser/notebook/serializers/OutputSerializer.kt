package org.jetbrains.jupyter.parser.notebook.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import org.jetbrains.jupyter.parser.notebook.DisplayData
import org.jetbrains.jupyter.parser.notebook.Error
import org.jetbrains.jupyter.parser.notebook.ExecuteResult
import org.jetbrains.jupyter.parser.notebook.Output
import org.jetbrains.jupyter.parser.notebook.Stream

public object OutputSerializer : KSerializer<Output> {
    override val descriptor: SerialDescriptor = serializer<JsonObject>().descriptor

    override fun deserialize(decoder: Decoder): Output {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement().jsonObject
        val format = decoder.json

        return when (val outputTypeString = element[OUTPUT_TYPE]?.decode<String>(format)) {
            "execute_result" -> {
                val data = element[DATA].decodeDisplayMap(format)
                val metadata = element[METADATA]?.decode<JsonObject>(format).orEmptyObject()
                val executionCount = element[EXECUTION_COUNT]?.decode<Long>(format)
                ExecuteResult(data, metadata, executionCount)
            }
            "display_data" -> {
                val data = element[DATA].decodeDisplayMap(format)
                val metadata = element[METADATA]?.decode<JsonObject>(format).orEmptyObject()
                DisplayData(data, metadata)
            }
            "stream" -> {
                val name = element[NAME]?.decode<String>(format).orEmpty()
                val text = element[TEXT].decodeMultilineText(format)
                Stream(name, text)
            }
            "error" -> {
                val ename = element[ENAME]?.decode<String>(format).orEmpty()
                val evalue = element[EVALUE]?.decode<String>(format).orEmpty()
                val traceback = element[TRACEBACK]?.decode<List<String>>(format).orEmpty()
                Error(ename, evalue, traceback)
            }
            else -> {
                throw SerializationException("Unknown output type: $outputTypeString")
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Output) {
        require(encoder is JsonEncoder)
        val format = encoder.json

        val json = buildJsonObject {
            put(OUTPUT_TYPE, JsonPrimitive(value.type.name.lowercase()))
            when (value) {
                is DisplayData -> {
                    put(DATA, format.encodeDisplayMap(value.data))
                    put(METADATA, format.encodeToJsonElement(value.metadata))
                    if (value is ExecuteResult) {
                        put(EXECUTION_COUNT, format.encodeToJsonElement(value.executionCount))
                    }
                }
                is Stream -> {
                    put(NAME, format.encodeToJsonElement(value.name))
                    put(TEXT, format.encodeMultilineText(value.text))
                }
                is Error -> {
                    put(ENAME, format.encodeToJsonElement(value.errorName))
                    put(EVALUE, format.encodeToJsonElement(value.errorValue))
                    put(TRACEBACK, format.encodeToJsonElement(value.traceback))
                }
            }
        }

        encoder.encodeJsonElement(json)
    }

    private const val OUTPUT_TYPE = "output_type"
    private const val DATA = "data"
    private const val METADATA = "metadata"
    private const val EXECUTION_COUNT = "execution_count"
    private const val NAME = "name"
    private const val TEXT = "text"
    private const val ENAME = "ename"
    private const val EVALUE = "evalue"
    private const val TRACEBACK = "traceback"
}
