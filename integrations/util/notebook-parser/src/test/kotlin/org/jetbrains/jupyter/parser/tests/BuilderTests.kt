package org.jetbrains.jupyter.parser.tests

import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import org.jetbrains.jupyter.builder.buildNotebook
import org.jetbrains.jupyter.parser.JupyterParser
import org.jetbrains.jupyter.parser.notebook.Cell
import org.jetbrains.jupyter.parser.notebook.CodeCell
import org.jetbrains.jupyter.parser.notebook.MarkdownCell
import org.jetbrains.jupyter.parser.notebook.RawCell
import org.junit.jupiter.api.Test

class BuilderTests {
    @Test
    fun `simple notebook`() {
        val actualNotebook = buildNotebook("kotlin", "Kotlin") {
            markdownCell("**Hello, world!**")
            codeCell("val x = 3")
        }

        actualNotebook.cells.forEach {
            val id = it.id
            id.shouldNotBeNull()
            id.length shouldBeInRange 1..64
            id.shouldMatch("^[a-zA-Z0-9-_]+$".toRegex())
        }

        val actual = JupyterParser.saveToJson(
            // cleaning up the random-generated ids
            actualNotebook.copy(cells = actualNotebook.cells.mapIndexed { i, cell -> cell.copy(id = "$i") })
        )

        val expected = testDataDir<BuilderTests>().resolve("simple.ipynb").readText()

        actual shouldBe expected
    }
}

private fun Cell.copy(id: String?): Cell = when (this) {
    is MarkdownCell -> MarkdownCell(
        id = id,
        metadata = this.metadata,
        source = this.source,
        attachments = this.attachments,
    )
    is RawCell -> RawCell(
        id = id,
        metadata = this.metadata,
        source = this.source,
        attachments = this.attachments,
    )
    is CodeCell -> CodeCell(
        id = id,
        metadata = this.metadata,
        source = this.source,
        outputs = this.outputs,
        executionCount = this.executionCount,
    )
}
