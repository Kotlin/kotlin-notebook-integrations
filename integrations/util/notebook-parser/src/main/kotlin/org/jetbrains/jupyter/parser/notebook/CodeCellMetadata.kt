package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.jetbrains.jupyter.parser.notebook.serializers.ScrolledSerializer

/**
 * Cell-level metadata.
 */
@Serializable
public class CodeCellMetadata(
    override val name: String? = null,
    override val tags: Set<String>? = null,
    override val jupyter: JsonObject? = null,
    /** Execution time for the code in the cell. This tracks time at which messages are received from iopub or shell channels */
    public val execution: Execution? = null,
    /** Whether the cell's output is collapsed/expanded. */
    public val collapsed: Boolean? = null,
    /** Whether the cell's output is scrolled, unscrolled, or autoscrolled. */
    public val scrolled: Scrolled? = null,
) : CellMetadata()

@Serializable(ScrolledSerializer::class)
public enum class Scrolled {
    SCROLLED,
    UNSCROLLED,
    AUTOSCROLLED,
}

/**
 * Execution time for the code in the cell. This tracks time at which messages are received from iopub or shell channels
 */
@Serializable
public data class Execution(
    /** header.date (in ISO 8601 format) of iopub channel's execute_input message. It indicates the time at which the kernel broadcasts an execute_input message to connected frontends */
    @SerialName("iopub.execute_input")
    val iopubExecuteInput: String? = null,

    /** header.date (in ISO 8601 format) of iopub channel's kernel status message when the status is 'busy' */
    @SerialName("iopub.status.busy")
    val iopubStatusBusy: String? = null,

    /** header.date (in ISO 8601 format) of iopub channel's kernel status message when the status is 'idle'. It indicates the time at which kernel finished processing the associated request */
    @SerialName("iopub.status.idle")
    val iopubStatusIdle: String? = null,

    /** header.date (in ISO 8601 format) of the shell channel's execute_reply message. It indicates the time at which the execute_reply message was created */
    @SerialName("shell.execute_reply")
    val shellExecuteReply: String? = null,
)
