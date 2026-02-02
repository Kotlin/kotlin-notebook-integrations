# Notebook Manipulator Integration

This integration provides programmatic access to manipulate the current Jupyter notebook from Kotlin kernel code. It uses the Jupyter Comms protocol to communicate with the frontend.

## Overview

The Notebook Manipulator integration enables you to:
- Query notebook structure (cell count, metadata, cells)
- Read cell content and metadata
- Insert, delete, and replace cells
- Modify notebook metadata
- Execute cells programmatically

## Usage

After loading the integration, use the `manipulateNotebook { }` DSL to access the notebook manipulator API. All operations inside the block are suspend functions.

```kotlin
%use notebook-manipulator

import org.jetbrains.jupyter.parser.notebook.CodeCell
import org.jetbrains.jupyter.parser.notebook.MarkdownCell
import org.jetbrains.jupyter.parser.notebook.CodeCellMetadata
import org.jetbrains.jupyter.parser.notebook.MarkdownCellMetadata

// Get the number of cells in the notebook
manipulateNotebook { getCellCount() }

// Get notebook metadata
manipulateNotebook { getNotebookMetadata() }

// Get all cells
manipulateNotebook { getAllCells() }

// Get a specific cell
manipulateNotebook { getCell(0) }

// Get a range of cells
manipulateNotebook { getCellRange(0, 5) }

// Insert a new cell at a specific position
val newCodeCell = CodeCell(
    id = null,
    source = "println(\"Hello from inserted cell!\")",
    metadata = CodeCellMetadata(),
    executionCount = null,
    outputs = emptyList()
)
manipulateNotebook { insertCell(2, newCodeCell) }

// Append a cell to the end
manipulateNotebook {
    val count = getCellCount()
    insertCell(count, newCodeCell)
}

// Delete a cell
manipulateNotebook { deleteCell(3) }

// Replace a cell
val markdownCell = MarkdownCell(
    id = null,
    source = "# Updated Section",
    metadata = MarkdownCellMetadata()
)
manipulateNotebook { replaceCell(1, markdownCell) }

// Update notebook metadata
manipulateNotebook {
    setNotebookMetadata(
        mapOf("custom_field" to "custom_value"),
        merge = true
    )
}

// Execute a specific cell
manipulateNotebook { executeCell(0) }

// Execute a range of cells
manipulateNotebook { executeCellRange(0, 3) }
```

## API Reference

### NotebookManipulator

The main interface for notebook manipulation. All methods are suspend functions and should be called within the `manipulateNotebook { }` block.

#### Query Operations

- `getCellCount(): Int` - Returns the total number of cells
- `getNotebookMetadata(): Map<String, Any?>` - Returns notebook metadata
- `getCell(index: Int): Cell` - Returns a single cell
- `getCellRange(start: Int, end: Int): List<Cell>` - Returns a range of cells (start inclusive, end exclusive)
- `getAllCells(): List<Cell>` - Returns all cells
- `getNotebook(): JupyterNotebook` - Returns the complete notebook structure

#### Modification Operations

- `insertCell(start: Int, cell: Cell)` - Inserts a cell at the specified position
- `insertCells(start: Int, cells: List<Cell>)` - Inserts multiple cells
- `appendCell(cell: Cell)` - Appends a cell to the end of the notebook
- `appendCells(cells: List<Cell>)` - Appends multiple cells to the end of the notebook
- `deleteCell(index: Int)` - Deletes a single cell
- `deleteCells(start: Int, deleteCount: Int)` - Deletes multiple cells
- `replaceCell(index: Int, cell: Cell)` - Replaces a single cell
- `replaceCells(start: Int, deleteCount: Int, cells: List<Cell>)` - Replaces multiple cells
- `spliceCells(start: Int, deleteCount: Int, cells: List<Cell>)` - Low-level splice operation
- `setNotebookMetadata(metadata: Map<String, Any?>, merge: Boolean = true)` - Updates notebook metadata

#### Execution Operations

- `executeCell(index: Int)` - Executes a single cell
- `executeCellRange(start: Int, end: Int)` - Executes a range of cells (start inclusive, end exclusive)
- `executeAllCells()` - Executes all cells in the notebook

### Cell Types

Use the `org.jetbrains.jupyter.parser.notebook` package types to create cells:

- `CodeCell(id, source, metadata, executionCount, outputs)` - A code cell
- `MarkdownCell(id, source, metadata)` - A markdown cell
- `RawCell(id, source, metadata)` - A raw cell

Each cell type has a corresponding metadata class:
- `CodeCellMetadata(tags)` - Metadata for code cells
- `MarkdownCellMetadata(tags)` - Metadata for markdown cells
- `RawCellMetadata(tags)` - Metadata for raw cells

When creating new cells, use `id = null` (the frontend will assign a proper ID).

## Protocol

The integration communicates with the frontend using the `jupyter.notebook.manipulator.v1` comm target. See [PROTOCOL.md](PROTOCOL.md) for the complete protocol specification.

## Requirements

- Jupyter frontend that implements the notebook manipulator protocol (e.g., IntelliJ IDEA with Kotlin Notebook plugin)
- Kotlin Jupyter kernel

## Module Structure

- `notebook-manipulator-api` - Core API and implementation
- `notebook-manipulator-jupyter` - Jupyter integration layer

## Error Handling

All operations throw `NotebookManipulatorException` if they fail. Common error scenarios:

- Invalid cell indices (out of bounds)
- Invalid range parameters (start > end)
- Communication timeout with frontend
- Frontend operation failures

## Examples

See the [notebooks](notebooks/) directory for complete examples demonstrating various use cases.
