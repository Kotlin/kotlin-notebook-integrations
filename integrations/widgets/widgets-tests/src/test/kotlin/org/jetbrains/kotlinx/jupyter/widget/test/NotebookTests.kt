package org.jetbrains.kotlinx.jupyter.widget.test

import org.jetbrains.jupyter.parser.JupyterParser
import org.jetbrains.jupyter.parser.notebook.Cell
import org.jetbrains.jupyter.parser.notebook.CodeCell
import org.jetbrains.jupyter.parser.notebook.Output
import org.jetbrains.kotlinx.jupyter.repl.result.EvalResultEx
import java.io.File
import kotlin.test.Test

class NotebookTests : AbstractWidgetReplTest() {
    @Test
    fun widgets() =
        notebookTest(
            notebookName = "widgets",
            replacer = {
                it.replace("WidgetArch.png", "$NOTEBOOK_EXAMPLES_PATH/WidgetArch.png")
            },
        )

    private fun notebookTest(
        notebookName: String,
        replacer: CodeReplacer = CodeReplacer.DEFAULT,
        cellClause: CellClause = CellClause.DEFAULT,
        cleanup: () -> Unit = {},
    ) {
        val path = "$NOTEBOOK_EXAMPLES_PATH/$notebookName.ipynb"
        doTest(path, replacer, cellClause, cleanup)
    }

    private fun doTest(
        notebookPath: String,
        replacer: CodeReplacer,
        cellClause: CellClause,
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

                // println("Executing code:\n$codeToExecute")
                val cellResult = execEx(codeToExecute)
                if (cellResult is EvalResultEx.AbstractError) {
                    throw cellResult.error
                }
                require(cellResult is EvalResultEx.Success)

                // println(cellResult)
            }
        } finally {
            cleanup()
        }
    }

    companion object {
        const val NOTEBOOK_EXAMPLES_PATH = "../notebooks"
    }
}

fun interface CodeReplacer {
    fun replace(code: String): String

    companion object {
        val DEFAULT = CodeReplacer { it }

        fun byMap(replacements: Map<String, String>) =
            CodeReplacer { code ->
                replacements.entries.fold(code) { acc, (key, replacement) ->
                    acc.replace(key, replacement)
                }
            }

        fun byMap(vararg replacements: Pair<String, String>): CodeReplacer = byMap(mapOf(*replacements))
    }
}

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

fun CellClause.Companion.stopAfter(breakClause: CellClause) =
    object : CellClause {
        var clauseTriggered: Boolean = false

        override fun isAccepted(cell: Cell): Boolean {
            clauseTriggered = clauseTriggered || breakClause.isAccepted(cell)
            return !clauseTriggered
        }
    }

data class CodeCellData(
    val code: String,
    val outputs: List<Output>,
)
