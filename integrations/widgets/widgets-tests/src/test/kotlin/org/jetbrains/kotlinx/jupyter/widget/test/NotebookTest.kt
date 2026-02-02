package org.jetbrains.kotlinx.jupyter.widget.test

import org.jetbrains.kotlinx.jupyter.test.util.CellClause
import org.jetbrains.kotlinx.jupyter.test.util.CodeReplacer
import org.jetbrains.kotlinx.jupyter.test.util.runNotebookTest
import org.junit.jupiter.api.Test

class NotebookTest : AbstractWidgetReplTest() {
    @Test
    fun `widgets notebook should run successfully`() =
        notebookTest(
            notebookName = "widgets",
            replacer = {
                it.replace("WidgetArch.png", "$NOTEBOOK_EXAMPLES_PATH/WidgetArch.png")
            },
        )

    @Test
    fun `output notebook should run successfully`() =
        notebookTest(
            notebookName = "output",
        )

    private fun notebookTest(
        notebookName: String,
        replacer: CodeReplacer = CodeReplacer.DEFAULT,
        cellClause: CellClause = CellClause.DEFAULT,
        cleanup: () -> Unit = {},
    ) {
        val path = "$NOTEBOOK_EXAMPLES_PATH/$notebookName.ipynb"
        runNotebookTest(path, replacer, cellClause, cleanup)
    }

    companion object {
        const val NOTEBOOK_EXAMPLES_PATH = "../notebooks"
    }
}
