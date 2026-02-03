package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.jetbrains.jupyter.parser.notebook.serializers.InstantSerializer
import org.jetbrains.jupyter.parser.notebook.serializers.ScrolledSerializer
import java.time.Instant

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
    /** Execution-related data, including compiled classes and other execution metadata */
    public val executionRelatedData: ExecutionRelatedData? = null,
    /** Execution time tracking with start and end timestamps */
    @SerialName("ExecuteTime")
    public val executeTime: ExecuteTime? = null,
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
    @Serializable(with = InstantSerializer::class)
    val iopubExecuteInput: Instant? = null,
    /** header.date (in ISO 8601 format) of iopub channel's kernel status message when the status is 'busy' */
    @SerialName("iopub.status.busy")
    @Serializable(with = InstantSerializer::class)
    val iopubStatusBusy: Instant? = null,
    /** header.date (in ISO 8601 format) of iopub channel's kernel status message when the status is 'idle'. It indicates the time at which kernel finished processing the associated request */
    @SerialName("iopub.status.idle")
    @Serializable(with = InstantSerializer::class)
    val iopubStatusIdle: Instant? = null,
    /** header.date (in ISO 8601 format) of the shell channel's execute_reply message. It indicates the time at which the execute_reply message was created */
    @SerialName("shell.execute_reply")
    @Serializable(with = InstantSerializer::class)
    val shellExecuteReply: Instant? = null,
)

/**
 * Execution-related data associated with cell execution
 */
@Serializable
public data class ExecutionRelatedData(
    /** List of compiled class names generated during cell execution */
    val compiledClasses: List<String>? = null,
)

/**
 * Execution time tracking for a cell
 */
@Serializable
public data class ExecuteTime(
    /** Start time of execution in ISO 8601 format */
    @SerialName("start_time")
    @Serializable(with = InstantSerializer::class)
    val startTime: Instant? = null,
    /** End time of execution in ISO 8601 format */
    @SerialName("end_time")
    @Serializable(with = InstantSerializer::class)
    val endTime: Instant? = null,
)
