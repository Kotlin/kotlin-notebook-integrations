package org.jetbrains.kotlinx.jupyter.notebook

import kotlinx.serialization.json.JsonObject
import org.jetbrains.jupyter.parser.notebook.Cell
import org.jetbrains.jupyter.parser.notebook.JupyterNotebook
import org.jetbrains.kotlinx.jupyter.notebook.protocol.NotebookFormatVersion
import java.io.Closeable

/**
 * Main API for working with the current Jupyter notebook.
 *
 * This interface provides methods to query and modify the notebook structure,
 * including cells and metadata. All methods are suspend functions to allow
 * asynchronous execution.
 */
public interface Notekit : Closeable {
    /**
     * Returns the total number of real notebook cells (including not executed,
     * excluding code snippets generated under the hood).
     *
     * @return The cell count
     * @throws NotekitException if the operation fails
     */
    public suspend fun getCellCount(): Int

    /**
     * Returns the metadata of the current notebook.
     *
     * @return The notebook metadata
     * @throws NotekitException if the operation fails
     */
    public suspend fun getNotebookMetadata(): JsonObject

    /**
     * Returns a range of cells from the current notebook.
     *
     * @param start The starting index (inclusive, 0-based)
     * @param end The ending index (exclusive, 0-based)
     * @return List of cells in the specified range
     * @throws NotekitException if the operation fails or range is invalid
     */
    public suspend fun getCellRange(
        start: Int,
        end: Int,
    ): List<Cell>

    /**
     * Returns a single cell from the current notebook.
     *
     * @param index The cell index (0-based)
     * @return The cell at the specified index
     * @throws NotekitException if the operation fails or index is invalid
     */
    public suspend fun getCell(index: Int): Cell = getCellRange(index, index + 1).first()

    /**
     * Returns all cells in the current notebook.
     *
     * @return List of all cells
     * @throws NotekitException if the operation fails
     */
    public suspend fun getAllCells(): List<Cell> {
        val count = getCellCount()
        return getCellRange(0, count)
    }

    /**
     * Returns the entire notebook structure including metadata and all cells.
     *
     * @return The complete notebook
     * @throws NotekitException if the operation fails
     */
    public suspend fun getNotebook(): JupyterNotebook

    /**
     * Deletes a range of cells from the notebook.
     *
     * @param start The starting index (inclusive, 0-based)
     * @param deleteCount The number of cells to delete
     * @throws NotekitException if the operation fails or parameters are invalid
     */
    public suspend fun deleteCells(
        start: Int,
        deleteCount: Int,
    ) {
        spliceCells(start, deleteCount, emptyList())
    }

    /**
     * Deletes a single cell from the notebook.
     *
     * @param index The index of the cell to delete (0-based)
     * @throws NotekitException if the operation fails or index is invalid
     */
    public suspend fun deleteCell(index: Int) {
        deleteCells(index, 1)
    }

    /**
     * Inserts cells at the specified position in the notebook.
     *
     * @param start The position where to insert cells (0-based)
     * @param cells The cells to insert
     * @throws NotekitException if the operation fails or start index is invalid
     */
    public suspend fun insertCells(
        start: Int,
        cells: List<Cell>,
    ) {
        spliceCells(start, 0, cells)
    }

    /**
     * Inserts a single cell at the specified position in the notebook.
     *
     * @param start The position where to insert the cell (0-based)
     * @param cell The cell to insert
     * @throws NotekitException if the operation fails or start index is invalid
     */
    public suspend fun insertCell(
        start: Int,
        cell: Cell,
    ) {
        insertCells(start, listOf(cell))
    }

    /**
     * Appends cells to the end of the notebook.
     *
     * @param cells The cells to append
     * @throws NotekitException if the operation fails
     */
    public suspend fun appendCells(cells: List<Cell>) {
        val count = getCellCount()
        insertCells(count, cells)
    }

    /**
     * Appends a single cell to the end of the notebook.
     *
     * @param cell The cell to append
     * @throws NotekitException if the operation fails
     */
    public suspend fun appendCell(cell: Cell) {
        appendCells(listOf(cell))
    }

    /**
     * Replaces a range of cells with new cells.
     *
     * @param start The starting index (inclusive, 0-based)
     * @param end The ending index (exclusive, 0-based)
     * @param cells The cells to insert at the start position
     * @throws NotekitException if the operation fails or parameters are invalid
     */
    public suspend fun replaceCells(
        start: Int,
        end: Int,
        cells: List<Cell>,
    ) {
        val deleteCount = end - start
        spliceCells(start, deleteCount, cells)
    }

    /**
     * Replaces a single cell with a new cell.
     *
     * @param index The index of the cell to replace (0-based)
     * @param cell The new cell
     * @throws NotekitException if the operation fails or index is invalid
     */
    public suspend fun replaceCell(
        index: Int,
        cell: Cell,
    ) {
        replaceCells(index, 1, listOf(cell))
    }

    /**
     * Performs a splice operation on the notebook cells.
     *
     * This is the low-level operation that combines deletion and insertion:
     * 1. Deletes `deleteCount` cells starting at index `start`
     * 2. Inserts all cells from the `cells` list at index `start`
     *
     * @param start The starting index (inclusive, 0-based)
     * @param deleteCount The number of cells to delete
     * @param cells The cells to insert
     * @throws NotekitException if the operation fails or parameters are invalid
     */
    public suspend fun spliceCells(
        start: Int,
        deleteCount: Int,
        cells: List<Cell>,
    )

    /**
     * Updates the notebook metadata.
     *
     * @param metadata The new metadata
     * @param merge If true, merges with existing metadata; if false, replaces entirely
     * @throws NotekitException if the operation fails
     */
    public suspend fun setNotebookMetadata(
        metadata: JsonObject,
        merge: Boolean = true,
    )

    /**
     * Executes a range of cells in the notebook.
     *
     * @param start The starting index (inclusive, 0-based)
     * @param end The ending index (exclusive, 0-based)
     * @throws NotekitException if the operation fails or range is invalid
     */
    public suspend fun executeCellRange(
        start: Int,
        end: Int,
    )

    /**
     * Executes a single cell in the notebook.
     *
     * @param index The cell index (0-based)
     * @throws NotekitException if the operation fails or index is invalid
     */
    public suspend fun executeCell(index: Int) {
        executeCellRange(index, index + 1)
    }

    /**
     * Executes all cells in the notebook.
     *
     * @throws NotekitException if the operation fails
     */
    public suspend fun executeAllCells() {
        val count = getCellCount()
        executeCellRange(0, count)
    }

    /**
     * Returns the notebook format version (nbformat major and minor).
     *
     * @return The notebook format version
     * @throws NotekitException if the operation fails
     */
    public suspend fun getNbFormatVersion(): NotebookFormatVersion
}
