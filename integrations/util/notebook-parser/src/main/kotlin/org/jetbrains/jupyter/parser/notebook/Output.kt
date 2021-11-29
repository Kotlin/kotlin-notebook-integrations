package org.jetbrains.jupyter.parser.notebook

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.jetbrains.jupyter.parser.notebook.serializers.OutputSerializer

@Serializable(OutputSerializer::class)
sealed class Output {
    abstract val type: Type

    enum class Type {
        EXECUTE_RESULT,
        DISPLAY_DATA,
        STREAM,
        ERROR,
    }
}

class ExecuteResult(
    data: JsonObject,
    metadata: JsonObject,
    /** A result's prompt number. */
    val executionCount: Long?,
) : DisplayData(data, metadata) {

    override val type: Type get() = Type.EXECUTE_RESULT

    init {
        if (executionCount != null)
            require(executionCount >= 0L) { "Execution count shouldn't be nagative, but it was $executionCount" }
    }
}

open class DisplayData(
    val data: JsonObject,
    val metadata: JsonObject,
) : Output() {
    override val type: Type get() = Type.DISPLAY_DATA
}

class Stream(
    /** The name of the stream (stdout, stderr). */
    val name: String,
    /** The stream's text output, represented as an array of strings. */
    val text: String
) : Output() {
    override val type: Type get() = Type.STREAM
}

class Error(
    /** The name of the error. */
    val errorName: String,
    /** The value, or message, of the error. */
    val errorValue: String,
    /** The error's traceback, represented as an array of strings. */
    val traceback: List<String>
) : Output() {
    override val type: Type get() = Type.ERROR
}
