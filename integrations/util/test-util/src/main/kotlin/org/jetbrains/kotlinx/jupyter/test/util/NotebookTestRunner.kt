package org.jetbrains.kotlinx.jupyter.test.util

import org.jetbrains.jupyter.parser.JupyterParser
import org.jetbrains.jupyter.parser.notebook.Cell
import org.jetbrains.jupyter.parser.notebook.CodeCell
import org.jetbrains.jupyter.parser.notebook.Output
import org.jetbrains.kotlinx.jupyter.repl.result.EvalResultEx
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import java.io.File

/**
 * A functional interface for transforming code before execution.
 */
fun interface CodeReplacer {
    fun replace(code: String): String

    companion object {
        val DEFAULT = CodeReplacer { it }
    }
}

/**
 * A functional interface for filtering cells to execute.
 */
fun interface CellClause {
    fun isAccepted(cell: Cell): Boolean

    companion object {
        val IS_NOT_SKIPTEST = CellClause { it.metadata.tags?.contains("skiptest") != true }
        val IS_CODE = CellClause { it.type == Cell.Type.CODE }
        val DEFAULT = IS_CODE and IS_NOT_SKIPTEST
    }
}

infix fun CellClause.and(other: CellClause): CellClause =
    CellClause { cell ->
        // Prevent lazy evaluation
        val acceptedThis = this.isAccepted(cell)
        val acceptedOther = other.isAccepted(cell)
        acceptedThis && acceptedOther
    }

/**
 * Data class representing a code cell with its source and outputs.
 */
data class CodeCellData(
    val code: String,
    val outputs: List<Output>,
)

/**
 * Runs a notebook test by executing all code cells that match the given clause.
 *
 * @param notebookPath Path to the notebook file
 * @param replacer Code replacer to transform code before execution
 * @param cellClause Filter for cells to execute
 * @param cleanup Cleanup function to run after the test (even on failure)
 */
fun JupyterReplTestCase.runNotebookTest(
    notebookPath: String,
    replacer: CodeReplacer = CodeReplacer.DEFAULT,
    cellClause: CellClause = CellClause.DEFAULT,
    cleanup: () -> Unit = {},
) {
    val notebookFile = File(notebookPath)
    val notebook = JupyterParser.parse(notebookFile)
    val finalClause = cellClause and CellClause.IS_CODE

    val codeCellsData =
        notebook.cells
            .filter { finalClause.isAccepted(it) }
            .map { CodeCellData(it.source, (it as? CodeCell)?.outputs.orEmpty()) }

    try {
        for (codeCellData in codeCellsData) {
            val code = codeCellData.code
            val codeToExecute = replacer.replace(code)

            val cellResult = execEx(codeToExecute)
            if (cellResult is EvalResultEx.AbstractError) {
                throw cellResult.error
            }
            require(cellResult is EvalResultEx.Success)
        }
    } finally {
        cleanup()
    }
}
