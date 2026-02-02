package org.jetbrains.jupyter.parser.notebook

public class CodeCell(
    override val id: String?,
    override val metadata: CodeCellMetadata,
    override val source: String,
    /** Execution, display, or stream outputs. */
    public val outputs: List<Output>,
    /** The code cell's prompt number. Will be null if the cell has not been run. */
    public val executionCount: Long?
) : Cell() {

    override val type: Type get() = Type.CODE

    init {
        if (executionCount != null)
            require(executionCount >= 0L) { "execution_count < minimum 0 - $executionCount" }
    }
}
