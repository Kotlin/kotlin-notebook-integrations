package org.jetbrains.kotlinx.jupyter.notebook.test

import org.jetbrains.kotlinx.jupyter.test.util.CellClause
import org.jetbrains.kotlinx.jupyter.test.util.CodeReplacer
import org.jetbrains.kotlinx.jupyter.test.util.runNotebookTest
import org.junit.jupiter.api.Test

class NotebookExamplesTest : AbstractNotebookManipulatorReplTest() {
    @Test
    fun `should run basic-usage notebook successfully`() = notebookTest(notebookName = "basic-usage")

    @Test
    fun `should run advanced-usage notebook successfully`() = notebookTest(notebookName = "advanced-usage")

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
