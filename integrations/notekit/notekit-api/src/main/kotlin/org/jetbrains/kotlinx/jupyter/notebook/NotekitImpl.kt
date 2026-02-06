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
import org.jetbrains.kotlinx.jupyter.notebook.protocol.ErrorInfo
import org.jetbrains.kotlinx.jupyter.notebook.protocol.ExecuteCellRangeRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.GetCellCountRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.GetCellRangeRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.GetNotebookMetadataRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.NotekitMessage
import org.jetbrains.kotlinx.jupyter.notebook.protocol.SetNotebookMetadataRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.SpliceCellRangeRequest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Implementation of [Notekit] that communicates with the frontend via Jupyter Comms.
 */
internal class NotekitImpl(
    private val commManager: CommManager,
    private val requestTimeout: Duration = 30.seconds,
) : Notekit {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    private val logger = Logger.getLogger(NotekitImpl::class.java.name)
    private val targetName = "jupyter.notebook.notekit.v1"
    private val requestIdCounter = AtomicLong(0)
    private val pendingRequests = ConcurrentHashMap<String, CompletableDeferred<JsonElement>>()

    private var comm: Comm? = null
    private val commLock = Any()

    override suspend fun getCellCount(): Int =
        request(::GetCellCountRequest) { result ->
            result.jsonObject.requireField<Int>("count")
        }

    override suspend fun getNotebookMetadata(): JsonObject =
        request(::GetNotebookMetadataRequest) { result ->
            result.jsonObject.requireJsonObject("metadata")
        }

    override suspend fun getCellRange(
        start: Int,
        end: Int,
    ): List<Cell> =
        request({ GetCellRangeRequest(it, start, end) }) { result ->
            result.jsonObject.requireJsonArray("cells").map { json.decodeFromJsonElement<Cell>(it) }
        }

    override suspend fun getNotebook(): JupyterNotebook {
        val notebookMetadata = getNotebookMetadata()
        val cells = getAllCells()
        val metadata = json.decodeFromJsonElement<Metadata>(notebookMetadata)

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
        val cellsJson = cells.map { json.encodeToJsonElement(it) }
        request { SpliceCellRangeRequest(it, start, deleteCount, cellsJson) }
    }

    override suspend fun setNotebookMetadata(
        metadata: JsonObject,
        merge: Boolean,
    ) {
        request { SetNotebookMetadataRequest(it, metadata, merge) }
    }

    override suspend fun executeCellRange(
        start: Int,
        end: Int,
    ) {
        request { ExecuteCellRangeRequest(it, start, end) }
    }

    private suspend fun <T> request(
        factory: (String) -> NotekitMessage,
        transform: (JsonElement) -> T,
    ): T {
        val requestId = generateRequestId()
        val request = factory(requestId)
        val deferred = CompletableDeferred<JsonElement>()
        pendingRequests[requestId] = deferred

        val result =
            try {
                ensureCommOpened().send(json.encodeToJsonElement(request).jsonObject)
                try {
                    withTimeout(requestTimeout) { deferred.await() }
                } finally {
                    pendingRequests.remove(requestId)
                }
            } catch (e: NotekitException) {
                throw e
            } catch (e: TimeoutCancellationException) {
                throw NotekitException("Request timed out: ${e.message}", "TIMEOUT", e)
            } catch (e: Exception) {
                throw NotekitException("Failed to send request: ${e.message}", cause = e)
            }

        return transform(result)
    }

    private suspend fun request(factory: (String) -> NotekitMessage) {
        request(factory) { }
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
        runCatching {
            val requestId =
                data["request_id"]?.jsonPrimitive?.content ?: run {
                    logger.log(Level.WARNING, "Response missing request_id field. Data: $data")
                    return
                }

            val deferred = pendingRequests.remove(requestId) ?: return

            when (data["status"]?.jsonPrimitive?.content) {
                "ok" ->
                    data["result"]?.let(deferred::complete)
                        ?: deferred.completeExceptionally(NotekitException("Response missing result field"))
                "error" -> {
                    val error =
                        data["error"]?.jsonObject?.let { json.decodeFromJsonElement<ErrorInfo>(it) }
                            ?: ErrorInfo("Response has error status but missing error field", "UNKNOWN")
                    deferred.completeExceptionally(NotekitException(error.message, error.code))
                }
                else ->
                    deferred.completeExceptionally(
                        NotekitException("Unknown response status: ${data["status"]}"),
                    )
            }
        }.onFailure { e ->
            logger.log(
                Level.WARNING,
                "Failed to parse or handle comm response. Data: $data. Individual requests will timeout if affected.",
                e,
            )
        }
    }

    private fun generateRequestId(): String = "req-${requestIdCounter.incrementAndGet()}"

    private inline fun <reified T> JsonObject.requireField(field: String): T =
        this[field]?.jsonPrimitive?.let {
            when (T::class) {
                Int::class -> it.int as T
                String::class -> it.content as T
                else -> throw NotekitException("Unsupported field type: ${T::class}")
            }
        } ?: throw NotekitException("Invalid response: missing '$field' field")

    private fun JsonObject.requireJsonObject(field: String): JsonObject =
        this[field]?.jsonObject ?: throw NotekitException("Invalid response: missing '$field' field")

    private fun JsonObject.requireJsonArray(field: String): List<JsonElement> =
        this[field]?.jsonArray ?: throw NotekitException("Invalid response: missing '$field' field")
}
