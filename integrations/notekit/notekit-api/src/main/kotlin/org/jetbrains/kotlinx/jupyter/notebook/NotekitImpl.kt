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
import kotlinx.serialization.json.long
import org.jetbrains.jupyter.parser.notebook.Cell
import org.jetbrains.jupyter.parser.notebook.JupyterNotebook
import org.jetbrains.jupyter.parser.notebook.Metadata
import org.jetbrains.kotlinx.jupyter.api.libraries.Comm
import org.jetbrains.kotlinx.jupyter.api.libraries.CommManager
import org.jetbrains.kotlinx.jupyter.notebook.protocol.ErrorInfo
import org.jetbrains.kotlinx.jupyter.notebook.protocol.ExecuteCellRangeRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_CELLS
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_COUNT
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_ERROR
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_METADATA
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_NBFORMAT
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_NBFORMAT_MINOR
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_REQUEST_ID
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_RESULT
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_STATUS
import org.jetbrains.kotlinx.jupyter.notebook.protocol.GetCellCountRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.GetCellRangeRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.GetNbFormatVersionRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.GetNotebookMetadataRequest
import org.jetbrains.kotlinx.jupyter.notebook.protocol.NOTEKIT_PROTOCOL_TARGET
import org.jetbrains.kotlinx.jupyter.notebook.protocol.NotebookFormatVersion
import org.jetbrains.kotlinx.jupyter.notebook.protocol.NotekitMessage
import org.jetbrains.kotlinx.jupyter.notebook.protocol.STATUS_ERROR
import org.jetbrains.kotlinx.jupyter.notebook.protocol.STATUS_OK
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
    private val requestIdCounter = AtomicLong(0)
    private val pendingRequests = ConcurrentHashMap<String, CompletableDeferred<JsonElement>>()

    private var comm: Comm? = null
    private val commLock = Any()

    override suspend fun getCellCount(): Int =
        request(::GetCellCountRequest) { result ->
            result.jsonObject.requireField<Int>(FIELD_COUNT)
        }

    override suspend fun getNotebookMetadata(): JsonObject =
        request(::GetNotebookMetadataRequest) { result ->
            result.jsonObject.requireJsonObject(FIELD_METADATA)
        }

    override suspend fun getCellRange(
        start: Int,
        end: Int,
    ): List<Cell> =
        request({ GetCellRangeRequest(it, start, end) }) { result ->
            result.jsonObject.requireJsonArray(FIELD_CELLS).map { json.decodeFromJsonElement<Cell>(it) }
        }

    override suspend fun getNotebook(): JupyterNotebook {
        val notebookMetadata = getNotebookMetadata()
        val cells = getAllCells()
        val metadata = json.decodeFromJsonElement<Metadata>(notebookMetadata)
        val nbFormatVersion = getNbFormatVersion()

        return JupyterNotebook(
            metadata = metadata,
            nbformatMinor = nbFormatVersion.minor,
            nbformat = nbFormatVersion.major,
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

    override suspend fun getNbFormatVersion(): NotebookFormatVersion =
        request(::GetNbFormatVersionRequest) { result ->
            val obj = result.jsonObject
            NotebookFormatVersion(
                major = obj.requireField(FIELD_NBFORMAT),
                minor = obj.requireField(FIELD_NBFORMAT_MINOR),
            )
        }

    override fun close() {
        synchronized(commLock) {
            comm?.close()
            comm = null
            pendingRequests.clear()
        }
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
                throw NotekitException("Request timed out: ${e.message}", NotekitErrorCode.TIMEOUT, e)
            } catch (e: Exception) {
                throw NotekitException("Failed to send request: ${e.message}", NotekitErrorCode.COMM_ERROR, e)
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
                    NOTEKIT_PROTOCOL_TARGET,
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
                data[FIELD_REQUEST_ID]?.jsonPrimitive?.content ?: run {
                    logger.log(Level.WARNING, "Response missing $FIELD_REQUEST_ID field. Data: $data")
                    return
                }

            val deferred = pendingRequests.remove(requestId) ?: return

            when (data[FIELD_STATUS]?.jsonPrimitive?.content) {
                STATUS_OK ->
                    data[FIELD_RESULT]?.let(deferred::complete)
                        ?: deferred.completeExceptionally(
                            NotekitException(
                                "Response missing $FIELD_RESULT field",
                                NotekitErrorCode.INVALID_RESPONSE,
                            ),
                        )
                STATUS_ERROR -> {
                    val error =
                        data[FIELD_ERROR]?.jsonObject?.let { json.decodeFromJsonElement<ErrorInfo>(it) }
                            ?: ErrorInfo("Response has error status but missing $FIELD_ERROR field", "UNKNOWN")
                    deferred.completeExceptionally(
                        NotekitException(
                            error.message,
                            NotekitErrorCode.fromString(error.code),
                        ),
                    )
                }
                else ->
                    deferred.completeExceptionally(
                        NotekitException("Unknown response status: ${data[FIELD_STATUS]}", NotekitErrorCode.INVALID_RESPONSE),
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
                Long::class -> it.long as T
                String::class -> it.content as T
                else -> throw NotekitException("Unsupported field type: ${T::class}", NotekitErrorCode.INVALID_RESPONSE)
            }
        } ?: throw NotekitException("Invalid response: missing '$field' field", NotekitErrorCode.INVALID_RESPONSE)

    private fun JsonObject.requireJsonObject(field: String): JsonObject =
        this[field]?.jsonObject ?: throw NotekitException("Invalid response: missing '$field' field", NotekitErrorCode.INVALID_RESPONSE)

    private fun JsonObject.requireJsonArray(field: String): List<JsonElement> =
        this[field]?.jsonArray ?: throw NotekitException("Invalid response: missing '$field' field", NotekitErrorCode.INVALID_RESPONSE)
}
