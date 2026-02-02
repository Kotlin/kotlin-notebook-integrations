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
import org.jetbrains.jupyter.parser.notebook.Cell
import org.jetbrains.jupyter.parser.notebook.CellWithAttachments
import org.jetbrains.jupyter.parser.notebook.CodeCell
import org.jetbrains.jupyter.parser.notebook.CodeCellMetadata
import org.jetbrains.jupyter.parser.notebook.MarkdownCell
import org.jetbrains.jupyter.parser.notebook.MarkdownCellMetadata
import org.jetbrains.jupyter.parser.notebook.Output
import org.jetbrains.jupyter.parser.notebook.RawCell
import org.jetbrains.jupyter.parser.notebook.RawCellMetadata

public object CellSerializer : KSerializer<Cell> {
    override val descriptor: SerialDescriptor = serializer<JsonObject>().descriptor

    override fun deserialize(decoder: Decoder): Cell {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement().jsonObject
        val format = decoder.json

        val id = element[ID]?.decode<String>(format)
        val cellTypeString = element[CELL_TYPE]?.decode<String>(format)
        val source = element[SOURCE].decodeMultilineText(format)
        val metadataJson = element[METADATA]

        return when (cellTypeString) {
            "code" -> {
                val metadata = metadataJson?.decode(format) ?: CodeCellMetadata()
                val outputs = element[OUTPUTS]?.decode<List<Output>>(format).orEmpty()
                val executionCount = element[EXECUTION_COUNT]?.decode<Long?>(format)

                CodeCell(id, metadata, source, outputs, executionCount)
            }
            "markdown" -> {
                val metadata = metadataJson?.decode(format) ?: MarkdownCellMetadata()
                val attachments = element[ATTACHMENTS]?.decode<JsonObject?>(format)

                MarkdownCell(id, metadata, source, attachments)
            }
            "raw" -> {
                val metadata = metadataJson?.decode(format) ?: RawCellMetadata()
                val attachments = element[ATTACHMENTS]?.decode<JsonObject?>(format)

                RawCell(id, metadata, source, attachments)
            }
            else -> {
                throw SerializationException("Unknown cell type: $cellTypeString")
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Cell) {
        require(encoder is JsonEncoder)
        val format = encoder.json

        val json = buildJsonObject {
            if (value.id != null) put(ID, JsonPrimitive(value.id))
            put(CELL_TYPE, JsonPrimitive(value.type.name.lowercase()))
            put(SOURCE, format.encodeMultilineText(value.source))

            @Suppress("REDUNDANT_ELSE_IN_WHEN")
            val metadata = when (value) {
                is CodeCell -> format.encodeToJsonElement(value.metadata)
                is MarkdownCell -> format.encodeToJsonElement(value.metadata)
                is RawCell -> format.encodeToJsonElement(value.metadata)
                else -> throw IllegalStateException("Impossible situation: there are no more types of cells")
            }
            put(METADATA, metadata)

            if (value is CodeCell) {
                put(OUTPUTS, format.encodeToJsonElement(value.outputs))
                put(EXECUTION_COUNT, format.encodeToJsonElement(value.executionCount))
            } else if (value is CellWithAttachments) {
                put(ATTACHMENTS, format.encodeToJsonElement(value.attachments))
            }
        }

        encoder.encodeJsonElement(json)
    }

    private const val ID = "id"
    private const val CELL_TYPE = "cell_type"
    private const val SOURCE = "source"
    private const val METADATA = "metadata"
    private const val OUTPUTS = "outputs"
    private const val EXECUTION_COUNT = "execution_count"
    private const val ATTACHMENTS = "attachments"
}
