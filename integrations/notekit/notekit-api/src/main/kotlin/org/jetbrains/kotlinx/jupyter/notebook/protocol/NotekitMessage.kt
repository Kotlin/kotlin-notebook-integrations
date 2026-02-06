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
@JsonClassDiscriminator("method")
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
 * Request to get the total number of cells in the notebook.
 */
@Serializable
@SerialName("get_cell_count")
internal data class GetCellCountRequest(
    @SerialName("request_id")
    override val requestId: String,
) : NotekitRequest

/**
 * Request to get notebook metadata.
 */
@Serializable
@SerialName("get_notebook_metadata")
internal data class GetNotebookMetadataRequest(
    @SerialName("request_id")
    override val requestId: String,
) : NotekitRequest

/**
 * Request to get a range of cells.
 */
@Serializable
@SerialName("get_cell_range")
internal data class GetCellRangeRequest(
    @SerialName("request_id")
    override val requestId: String,
    val params: CellRangeParams,
) : NotekitRequest

@Serializable
internal data class CellRangeParams(
    val start: Int,
    val end: Int,
)

/**
 * Request to splice (modify) a range of cells.
 */
@Serializable
@SerialName("splice_cell_range")
internal data class SpliceCellRangeRequest(
    @SerialName("request_id")
    override val requestId: String,
    val params: SpliceCellRangeParams,
) : NotekitRequest

@Serializable
internal data class SpliceCellRangeParams(
    val start: Int,
    @SerialName("delete_count")
    val deleteCount: Int,
    val cells: List<JsonElement>,
)

/**
 * Request to set notebook metadata.
 */
@Serializable
@SerialName("set_notebook_metadata")
internal data class SetNotebookMetadataRequest(
    @SerialName("request_id")
    override val requestId: String,
    val params: SetNotebookMetadataParams,
) : NotekitRequest

@Serializable
internal data class SetNotebookMetadataParams(
    val metadata: JsonElement,
    val merge: Boolean = true,
)

/**
 * Request to execute a range of cells.
 */
@Serializable
@SerialName("execute_cell_range")
internal data class ExecuteCellRangeRequest(
    @SerialName("request_id")
    override val requestId: String,
    val params: CellRangeParams,
) : NotekitRequest
