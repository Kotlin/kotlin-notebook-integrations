package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.jetbrains.jupyter.parser.notebook.serializers.OutputSerializer

@Serializable(OutputSerializer::class)
public sealed class Output {
    public abstract val type: Type

    public enum class Type {
        EXECUTE_RESULT,
        DISPLAY_DATA,
        STREAM,
        ERROR,
    }
}

public class ExecuteResult(
    data: Map<String, String>,
    metadata: JsonObject,
    /** A result's prompt number. */
    public val executionCount: Long?,
) : DisplayData(data, metadata) {

    override val type: Type get() = Type.EXECUTE_RESULT

    init {
        if (executionCount != null)
            require(executionCount >= 0L) { "Execution count shouldn't be nagative, but it was $executionCount" }
    }
}

public open class DisplayData(
    public val data: Map<String, String>,
    public val metadata: JsonObject,
) : Output() {
    override val type: Type get() = Type.DISPLAY_DATA

    public operator fun get(mimeType: String): String? = data[mimeType]
}

public class Stream(
    /** The name of the stream (stdout, stderr). */
    public val name: String,
    /** The stream's text output, represented as an array of strings. */
    public val text: String
) : Output() {
    override val type: Type get() = Type.STREAM
}

public class Error(
    /** The name of the error. */
    public val errorName: String,
    /** The value, or message, of the error. */
    public val errorValue: String,
    /** The error's traceback, represented as an array of strings. */
    public val traceback: List<String>
) : Output() {
    override val type: Type get() = Type.ERROR
}
