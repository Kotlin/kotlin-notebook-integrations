### Notebook Manipulator Project Requirements & Guidelines

This document summarizes the key architectural decisions, requirements, and technical details for the Notebook Manipulator integration. This information is intended for other agents working on this project.

#### 1. Core Architecture

- **Modules & Project Structure**:
    - `notebook-manipulator-api`: Core logic, protocol implementation, and the `NotebookManipulator` interface with its implementation.
    - `notebook-manipulator-jupyter`: Integration with Kotlin Jupyter Notebook. Contains the Jupyter-specific integration code that registers `notebookManipulator` in the notebook scope.
    - `notebook-manipulator-tests`: Integration and REPL tests.
- **Comm Target**:
    - `jupyter.notebook.manipulator.v1`: Target for all notebook manipulation requests.
- **Request/Response Pattern**: The protocol uses a synchronous request-response pattern over Jupyter Comms.
    - Each request includes a unique `request_id` that is echoed in the response.
    - Responses include a `status` field (`"ok"` or `"error"`).
    - All requests block until a response is received or a timeout occurs.

#### 2. Protocol Details

See [PROTOCOL.md](PROTOCOL.md) for the complete protocol specification.

- **Message Structure**:
    - Requests: `{ "method": "<method-name>", "request_id": "<unique-id>", "params": {...} }`
    - Responses: `{ "request_id": "<same-id>", "status": "ok"|"error", "result": {...}, "error": {...} }`
- **Methods**:
    - `get_cell_count`: Returns the total number of cells.
    - `get_notebook_metadata`: Returns notebook metadata.
    - `get_cell_range`: Returns a range of cells (start inclusive, end exclusive).
    - `splice_cell_range`: Modifies cells (delete, insert, or replace).
    - `set_notebook_metadata`: Updates notebook metadata with optional merge.
    - `execute_cell_range`: Executes a range of cells.
- **Error Codes**: `INVALID_RANGE`, `OUT_OF_BOUNDS`, `INVALID_SPLICE_PARAMS`, `INVALID_METADATA`, `INVALID_CELL_DATA`, `EXECUTION_FAILED`, `UNKNOWN_METHOD`, `INTERNAL_ERROR`, `NO_ACTIVE_NOTEBOOK`.

#### 3. JSON Conversion Utilities

- **Location**: `JsonConversions.kt` in `notebook-manipulator-api`.
- **Functions**:
    - `anyToJson(value: Any?): JsonElement`: Converts any Kotlin value to JSON (maps, lists, primitives, null).
    - `mapToJson(map: Map<String, Any?>): JsonElement`: Converts a map to `JsonObject`.
    - `listToJson(list: List<*>): JsonElement`: Converts a list to `JsonArray`.
    - `jsonToMap(json: JsonObject): Map<String, Any?>`: Converts `JsonObject` to a map.
    - `jsonToAny(element: JsonElement): Any?`: Converts any `JsonElement` to Kotlin types.
- **Type Handling**:
    - Primitives (String, Number, Boolean) serialize directly.
    - `null` serializes to `JsonNull`.
    - Unsupported types are converted via `toString()`.

#### 4. Cell Types

Use the `org.jetbrains.jupyter.parser.notebook` package types from the `jupyter-notebooks-parser` library:

- `CodeCell(id, source, metadata, executionCount, outputs)`: A code cell.
- `MarkdownCell(id, source, metadata)`: A markdown cell.
- `RawCell(id, source, metadata)`: A raw cell.

Each cell type has a corresponding metadata class:
- `CodeCellMetadata(tags)`: Metadata for code cells.
- `MarkdownCellMetadata(tags)`: Metadata for markdown cells.
- `RawCellMetadata(tags)`: Metadata for raw cells.

When creating new cells, use `id = ""` (the frontend will assign a proper ID).

#### 5. Testing Requirements

- **Assertions**: EXCLUSIVELY use Kotest `should*` notation (e.g., `result shouldBe 42`, `result.shouldBeInstanceOf<Type>()`). Do not use `assert*`, `assertEquals`, `assertTrue`, etc.
- **JSON creation**: EXCLUSIVELY use `buildJsonObject` and `buildJsonArray` for creating JSON objects and arrays.
- **Naming**: Test method names MUST start with `should` and use backticks (e.g., `fun \`should send correct getCellCount request\`()`). Do NOT use `test` prefix.
- **REPL Tests**: Inherit from `AbstractNotebookManipulatorReplTest`. Use `shouldHaveNextOpenEvent`, `shouldHaveNextMessageEvent`, `shouldHaveMethod`, `shouldHaveRequestId`, etc., to verify the sequence of Comm events.
- **Notebook Example Tests**: Test example notebooks by parsing and executing each code cell. See `NotebookExamplesTest.kt`.

#### 6. Build & Style

- **Gradle Tasks**:
    - `:compileKotlin`: Verifies that code compiles correctly.
    - `:ktlintFormat`: Automatically formats the code according to the project's style guide.
    - `:check`: Runs all verification tasks, including tests and ktlint checks.
- **Dependencies**:
    - `jupyter-notebooks-parser`: For parsing and representing notebook cells.
    - `kotlinx-serialization-json`: For JSON serialization.
    - `kotest-assertions-core` (via `test.kotlintest.assertions`): For test assertions.
- **KDocs**: Always use multiline format:
    ```kotlin
    /**
     * Description here.
     */
    ```

#### 7. API Usage

After loading the integration with `%use notebook-manipulator`, use the `manipulateNotebook { }` DSL to access the API. All operations inside the block are suspend functions.

```kotlin
import org.jetbrains.jupyter.parser.notebook.CodeCell
import org.jetbrains.jupyter.parser.notebook.MarkdownCell
import org.jetbrains.jupyter.parser.notebook.CodeCellMetadata
import org.jetbrains.jupyter.parser.notebook.MarkdownCellMetadata

// Query operations
manipulateNotebook { getCellCount() }
manipulateNotebook { getNotebookMetadata() }
manipulateNotebook { getCell(0) }
manipulateNotebook { getCellRange(0, 5) }
manipulateNotebook { getAllCells() }
manipulateNotebook { getNotebook() }

// Modification operations
val newCell = CodeCell(
    id = "",
    source = "println(\"Hello\")",
    metadata = CodeCellMetadata(),
    executionCount = null,
    outputs = emptyList()
)
manipulateNotebook { insertCell(index, newCell) }
manipulateNotebook { appendCell(newCell) }
manipulateNotebook { deleteCell(index) }
manipulateNotebook { replaceCell(index, newCell) }
manipulateNotebook { setNotebookMetadata(map, merge = true) }

// Execution operations
manipulateNotebook { executeCell(0) }
manipulateNotebook { executeCellRange(0, 3) }
manipulateNotebook { executeAllCells() }
```

#### 8. Error Handling

- All operations throw `NotebookManipulatorException` on failure.
- The exception includes an `errorCode` field matching the protocol error codes.
- Timeouts result in an exception (default timeout is configurable).
