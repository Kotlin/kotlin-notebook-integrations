package org.jetbrains.kotlinx.jupyter.notebook.protocol

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement

/**
 * Base interface for all notekit protocol messages.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator(FIELD_METHOD)
@Serializable
internal sealed interface NotekitMessage

/**
 * Base interface for request messages sent from kernel to frontend.
 */
@Serializable
internal sealed interface NotekitRequest : NotekitMessage {
    val requestId: String
}

@Serializable
internal data class ErrorInfo(
    val message: String,
    val code: String,
)

/**
 * Request to get the total number of real notebook cells (including not executed,
 * excluding code snippets generated under the hood).
 */
@Serializable
@SerialName(METHOD_GET_CELL_COUNT)
internal data class GetCellCountRequest(
    @SerialName(FIELD_REQUEST_ID)
    override val requestId: String,
) : NotekitRequest

/**
 * Request to get notebook metadata.
 */
@Serializable
@SerialName(METHOD_GET_NOTEBOOK_METADATA)
internal data class GetNotebookMetadataRequest(
    @SerialName(FIELD_REQUEST_ID)
    override val requestId: String,
) : NotekitRequest

/**
 * Request to get a range of cells.
 */
@Serializable
@SerialName(METHOD_GET_CELL_RANGE)
internal data class GetCellRangeRequest(
    @SerialName(FIELD_REQUEST_ID)
    override val requestId: String,
    val start: Int,
    val end: Int,
) : NotekitRequest

/**
 * Request to splice (modify) a range of cells.
 */
@Serializable
@SerialName(METHOD_SPLICE_CELL_RANGE)
internal data class SpliceCellRangeRequest(
    @SerialName(FIELD_REQUEST_ID)
    override val requestId: String,
    val start: Int,
    @SerialName(FIELD_DELETE_COUNT)
    val deleteCount: Int,
    val cells: List<JsonElement>,
) : NotekitRequest

/**
 * Request to set notebook metadata.
 */
@Serializable
@SerialName(METHOD_SET_NOTEBOOK_METADATA)
internal data class SetNotebookMetadataRequest(
    @SerialName(FIELD_REQUEST_ID)
    override val requestId: String,
    val metadata: JsonElement,
    val merge: Boolean = true,
) : NotekitRequest

/**
 * Request to execute a range of cells.
 */
@Serializable
@SerialName(METHOD_EXECUTE_CELL_RANGE)
internal data class ExecuteCellRangeRequest(
    @SerialName(FIELD_REQUEST_ID)
    override val requestId: String,
    val start: Int,
    val end: Int,
) : NotekitRequest

/**
 * Request to get the notebook format version.
 */
@Serializable
@SerialName(METHOD_GET_NBFORMAT_VERSION)
internal data class GetNbFormatVersionRequest(
    @SerialName(FIELD_REQUEST_ID)
    override val requestId: String,
) : NotekitRequest
