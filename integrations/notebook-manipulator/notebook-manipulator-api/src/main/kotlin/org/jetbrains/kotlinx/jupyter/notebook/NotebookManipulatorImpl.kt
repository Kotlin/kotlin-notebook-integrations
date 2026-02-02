package org.jetbrains.kotlinx.jupyter.notebook

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.jupyter.parser.notebook.Cell
import org.jetbrains.jupyter.parser.notebook.JupyterNotebook
import org.jetbrains.jupyter.parser.notebook.Metadata
import org.jetbrains.kotlinx.jupyter.api.libraries.Comm
import org.jetbrains.kotlinx.jupyter.api.libraries.CommManager
import org.jetbrains.kotlinx.jupyter.notebook.protocol.CellRangeParams
import org.jetbrains.kotlinx.jupyter.notebook.protocol.ErrorInfo
import org.jetbrains.kotlinx.jupyter.notebook.protocol.ExecuteCellRangeRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.GetCellCountRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.GetCellRangeRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.GetNotebookMetadataRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.NotebookManipulatorMessage
import org.jetbrains.kotlinx.jupyter.notebook.protocol.SetNotebookMetadataParams
import org.jetbrains.kotlinx.jupyter.notebook.protocol.SetNotebookMetadataRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.SpliceCellRangeParams
import org.jetbrains.kotlinx.jupyter.notebook.protocol.SpliceCellRangeRequest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Implementation of [NotebookManipulator] that communicates with the frontend via Jupyter Comms.
 */
internal class NotebookManipulatorImpl(
    private val commManager: CommManager,
    private val requestTimeout: Duration = 30.seconds,
) : NotebookManipulator {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val logger = Logger.getLogger(NotebookManipulatorImpl::class.java.name)
    private val targetName = "jupyter.notebook.manipulator.v1"
    private val requestIdCounter = AtomicLong(0)
    private val pendingRequests = ConcurrentHashMap<String, CompletableDeferred<JsonElement>>()

    private var comm: Comm? = null
    private val commLock = Any()

    override suspend fun getCellCount(): Int {
        val requestId = generateRequestId()
        val request = GetCellCountRequest(requestId)

        return sendRequest(request, requestId) { result ->
            result.jsonObject["count"]?.jsonPrimitive?.int
                ?: throw NotebookManipulatorException("Invalid response: missing 'count' field")
        }
    }

    override suspend fun getNotebookMetadata(): Map<String, Any?> {
        val requestId = generateRequestId()
        val request = GetNotebookMetadataRequest(requestId)

        return sendRequest(request, requestId) { result ->
            val metadata =
                result.jsonObject["metadata"]?.jsonObject
                    ?: throw NotebookManipulatorException("Invalid response: missing 'metadata' field")
            jsonToMap(metadata)
        }
    }

    override suspend fun getCellRange(
        start: Int,
        end: Int,
    ): List<Cell> {
        val requestId = generateRequestId()
        val request =
            GetCellRangeRequest(
                requestId,
                CellRangeParams(start, end),
            )

        return sendRequest(request, requestId) { result ->
            val cells =
                result.jsonObject["cells"]?.jsonArray
                    ?: throw NotebookManipulatorException("Invalid response: missing 'cells' field")

            cells.map { cellJson ->
                json.decodeFromJsonElement<Cell>(cellJson)
            }
        }
    }

    override suspend fun getNotebook(): JupyterNotebook {
        val metadataMap = getNotebookMetadata()
        val cells = getAllCells()

        // Convert the metadata map to the Metadata object
        // For now, we create a minimal Metadata. Frontend may provide more fields.
        val metadataJson = mapToJson(metadataMap)
        val metadata = json.decodeFromJsonElement<Metadata>(metadataJson)

        return JupyterNotebook(
            metadata = metadata,
            nbformatMinor = 5,
            nbformat = 4,
            cells = cells,
        )
    }

    override suspend fun spliceCells(
        start: Int,
        deleteCount: Int,
        cells: List<Cell>,
    ) {
        val requestId = generateRequestId()
        val cellsJson = cells.map { cell -> serializeCell(cell) }

        val request =
            SpliceCellRangeRequest(
                requestId,
                SpliceCellRangeParams(start, deleteCount, cellsJson),
            )

        sendRequest(request, requestId) { result ->
            // We don't need the result, just check that it succeeded
            result
        }
    }

    override suspend fun setNotebookMetadata(
        metadata: Map<String, Any?>,
        merge: Boolean,
    ) {
        val requestId = generateRequestId()
        val metadataJson = mapToJson(metadata)

        val request =
            SetNotebookMetadataRequest(
                requestId,
                SetNotebookMetadataParams(metadataJson, merge),
            )

        sendRequest(request, requestId) { result ->
            // We don't need the result, just check that it succeeded
            result
        }
    }

    override suspend fun executeCellRange(
        start: Int,
        end: Int,
    ) {
        val requestId = generateRequestId()
        val request =
            ExecuteCellRangeRequest(
                requestId,
                CellRangeParams(start, end),
            )

        sendRequest(request, requestId) { result ->
            // We don't need the result, just check that it succeeded
            result
        }
    }

    private suspend fun <T> sendRequest(
        request: NotebookManipulatorMessage,
        requestId: String,
        transform: (JsonElement) -> T,
    ): T {
        val deferred = CompletableDeferred<JsonElement>()
        pendingRequests[requestId] = deferred

        try {
            val comm = ensureCommOpened()
            val message = json.encodeToJsonElement(request).jsonObject
            comm.send(message)

            val result =
                try {
                    withTimeout(requestTimeout) {
                        deferred.await()
                    }
                } catch (e: TimeoutCancellationException) {
                    pendingRequests.remove(requestId)
                    throw NotebookManipulatorException(
                        "Request timed out: ${e.message}",
                        "TIMEOUT",
                        e,
                    )
                }

            return transform(result)
        } catch (e: NotebookManipulatorException) {
            throw e
        } catch (e: Exception) {
            pendingRequests.remove(requestId)
            throw NotebookManipulatorException("Failed to send request: ${e.message}", cause = e)
        }
    }

    private fun ensureCommOpened(): Comm =
        synchronized(commLock) {
            comm?.let { return it }

            val newComm =
                commManager.openComm(
                    targetName,
                    buildJsonObject {},
                    buildJsonObject {},
                    emptyList(),
                )

            newComm.onMessage { data, _, _ ->
                handleResponse(data)
            }

            comm = newComm
            newComm
        }

    private fun handleResponse(data: JsonObject) {
        try {
            val requestId =
                data["request_id"]?.jsonPrimitive?.content
                    ?: run {
                        logger.log(Level.WARNING, "Response missing request_id field. Data: $data")
                        return
                    }

            val future = pendingRequests.remove(requestId) ?: return

            when (val statusString = data["status"]?.jsonPrimitive?.content) {
                "ok" -> {
                    val result =
                        data["result"]
                            ?: run {
                                future.completeExceptionally(
                                    NotebookManipulatorException("Response missing result field"),
                                )
                                return
                            }
                    future.complete(result)
                }
                "error" -> {
                    val errorJson = data["error"]?.jsonObject
                    if (errorJson != null) {
                        val errorInfo = json.decodeFromJsonElement<ErrorInfo>(errorJson)
                        future.completeExceptionally(
                            NotebookManipulatorException(errorInfo.message, errorInfo.code),
                        )
                    } else {
                        future.completeExceptionally(
                            NotebookManipulatorException("Response has error status but missing error field"),
                        )
                    }
                }
                else -> {
                    future.completeExceptionally(
                        NotebookManipulatorException("Unknown response status: $statusString"),
                    )
                }
            }
        } catch (e: Exception) {
            logger.log(
                Level.WARNING,
                "Failed to parse or handle comm response. Data: $data. Individual requests will timeout if affected.",
                e,
            )
        }
    }

    private fun generateRequestId(): String = "req-${requestIdCounter.incrementAndGet()}"

    private fun serializeCell(cell: Cell): JsonElement = json.encodeToJsonElement(cell)
}
