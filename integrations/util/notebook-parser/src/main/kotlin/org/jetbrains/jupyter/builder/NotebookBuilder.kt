package org.jetbrains.jupyter.builder

import org.intellij.lang.annotations.Language
import org.jetbrains.jupyter.parser.notebook.CodeCell
import org.jetbrains.jupyter.parser.notebook.CodeCellMetadata
import org.jetbrains.jupyter.parser.notebook.JupyterNotebook
import org.jetbrains.jupyter.parser.notebook.KernelSpec
import org.jetbrains.jupyter.parser.notebook.LanguageInfo
import org.jetbrains.jupyter.parser.notebook.MarkdownCell
import org.jetbrains.jupyter.parser.notebook.MarkdownCellMetadata
import org.jetbrains.jupyter.parser.notebook.Metadata
import java.lang.Long
import java.security.SecureRandom

public fun buildNotebook(
    languageName: String,
    languageDisplayName: String,
    build: NotebookBuilder.() -> Unit,
): JupyterNotebook = NotebookBuilder(
    languageName = languageName,
    languageDisplayName = languageDisplayName,
).apply(build).build()

public class NotebookBuilder(
    private val languageName: String,
    private val languageDisplayName: String,
) {
    private val cells: MutableList<CellBuilder> = mutableListOf()

    public fun codeCell(code: String) {
        cells.add(CellBuilder.Code(code))
    }

    public fun markdownCell(@Language("Markdown") markdown: String) {
        cells.add(CellBuilder.Markdown(markdown))
    }

    private sealed class CellBuilder(val source: String) {
        class Code(content: String) : CellBuilder(content)
        class Markdown(content: String) : CellBuilder(content)
    }

    public fun build(): JupyterNotebook {
        return JupyterNotebook(
            metadata = Metadata(
                kernelSpec = KernelSpec(
                    name = languageName,
                    displayName = languageDisplayName,
                ),
                languageInfo = LanguageInfo(
                    name = languageName,
                ),
            ),
            nbformatMinor = 5,
            nbformat = 4,
            cells = cells.map {
                when (it) {
                    is CellBuilder.Code -> CodeCell(
                        id = CellIdGenerator.generateCellId(),
                        metadata = CodeCellMetadata(),
                        source = it.source,
                        outputs = emptyList(),
                        executionCount = null,
                    )
                    is CellBuilder.Markdown -> MarkdownCell(
                        id = CellIdGenerator.generateCellId(),
                        metadata = MarkdownCellMetadata(),
                        source = it.source,
                    )
                }
            }
        )
    }
}

private object CellIdGenerator {
    private val random = SecureRandom()

    fun generateCellId(): String {
        return Long.toHexString(random.nextLong())
    }
}
