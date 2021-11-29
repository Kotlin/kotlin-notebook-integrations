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
import org.jetbrains.jupyter.parser.notebook.Cell
import org.jetbrains.jupyter.parser.notebook.CodeCell
import org.jetbrains.jupyter.parser.notebook.CodeCellMetadata
import org.jetbrains.jupyter.parser.notebook.MarkdownCell
import org.jetbrains.jupyter.parser.notebook.MarkdownCellMetadata
import org.jetbrains.jupyter.parser.notebook.Output
import org.jetbrains.jupyter.parser.notebook.RawCell
import org.jetbrains.jupyter.parser.notebook.RawCellMetadata
import org.jetbrains.jupyter.parser.notebook.decode
import org.jetbrains.jupyter.parser.notebook.decodeMultilineText

object CellSerializer : KSerializer<Cell> {
    override val descriptor: SerialDescriptor = serializer<JsonObject>().descriptor

    override fun deserialize(decoder: Decoder): Cell {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement().jsonObject
        val format = decoder.json

        val id = element["id"]?.decode<String>(format)
        val cellTypeString = element["cell_type"]?.decode<String>(format)

        val source = element["source"].decodeMultilineText(format)

        val metadataJson = element["metadata"]

        return when (cellTypeString) {
            "code" -> {
                val metadata = metadataJson?.decode(format) ?: CodeCellMetadata()
                val outputs = element["outputs"]?.decode<List<Output>>(format).orEmpty()
                val executionCount = element["execution_count"]?.decode<Long?>(format)

                CodeCell(id, metadata, source, outputs, executionCount)
            }
            "markdown" -> {
                val metadata = metadataJson?.decode(format) ?: MarkdownCellMetadata()
                val attachments = element["attachments"]?.decode<JsonObject?>(format)

                MarkdownCell(id, metadata, source, attachments)
            }
            "raw" -> {
                val metadata = metadataJson?.decode(format) ?: RawCellMetadata()
                val attachments = element["attachments"]?.decode<JsonObject?>(format)

                RawCell(id, metadata, source, attachments)
            }
            else -> {
                throw SerializationException("Unknown cell type: $cellTypeString")
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Cell) {
    }
}
