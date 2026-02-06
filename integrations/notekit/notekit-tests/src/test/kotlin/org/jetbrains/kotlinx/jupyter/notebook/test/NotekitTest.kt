package org.jetbrains.kotlinx.jupyter.notebook.test

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_CELLS
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_COUNT
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_DELETE_COUNT
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_END
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_MERGE
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_METADATA
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_START
import org.jetbrains.kotlinx.jupyter.notebook.protocol.METHOD_GET_CELL_COUNT
import org.jetbrains.kotlinx.jupyter.notebook.protocol.METHOD_GET_CELL_RANGE
import org.jetbrains.kotlinx.jupyter.notebook.protocol.METHOD_GET_NOTEBOOK_METADATA
import org.jetbrains.kotlinx.jupyter.notebook.protocol.METHOD_SET_NOTEBOOK_METADATA
import org.jetbrains.kotlinx.jupyter.notebook.protocol.METHOD_SPLICE_CELL_RANGE
import org.jetbrains.kotlinx.jupyter.notebook.protocol.NOTEKIT_PROTOCOL_TARGET
import org.jetbrains.kotlinx.jupyter.notebook.protocol.STATUS_OK
import org.junit.jupiter.api.Test

class NotekitTest : AbstractNotekitReplTest() {
    @Test
    fun `should send correct getCellCount request and handle response`() {
        // 1. Start async operation (runs in thread INSIDE REPL)
        val resultVar = runNotekit("getCellCount()")

        // 2. Verify comm events in order
        val (commId) = shouldHaveNextOpenEvent(NOTEKIT_PROTOCOL_TARGET)

        val requestId =
            shouldHaveNextMessageEvent()
                .shouldHaveMethod(METHOD_GET_CELL_COUNT)
                .shouldHaveRequestId()

        // 4. Send response to unblock the operation
        sendResponse(
            commId,
            requestId,
            status = STATUS_OK,
            result =
                buildResult {
                    put(FIELD_COUNT, 10)
                },
        )

        // 5. Wait for completion and verify result
        val result = awaitAsyncResult<Int>(resultVar)
        result shouldBe 10
    }

    @Test
    fun `should send correct getNotebookMetadata request`() {
        // 1. Start async operation (runs in thread INSIDE REPL)
        val resultVar = runNotekit("getNotebookMetadata()")

        // 2. Verify request is sent correctly
        val (commId) = shouldHaveNextOpenEvent(NOTEKIT_PROTOCOL_TARGET)

        val requestId =
            shouldHaveNextMessageEvent()
                .shouldHaveMethod(METHOD_GET_NOTEBOOK_METADATA)
                .shouldHaveRequestId()

        // 4. Send response with metadata
        sendResponse(
            commId,
            requestId,
            status = STATUS_OK,
            result =
                buildResult {
                    putJsonObject(FIELD_METADATA) {
                        put("kernelspec", "python3")
                    }
                },
        )

        // 5. Wait for completion
        awaitAsyncResult<Any?>(resultVar)
    }

    @Test
    fun `should send correct getCellRange request with start and end params`() {
        // 1. Start async operation (runs in thread INSIDE REPL)
        val resultVar = runNotekit("getCellRange(0, 2)")

        // 2. Verify request is sent correctly
        val (commId) = shouldHaveNextOpenEvent(NOTEKIT_PROTOCOL_TARGET)

        val msgEvent =
            shouldHaveNextMessageEvent()
                .shouldHaveMethod(METHOD_GET_CELL_RANGE)
        val requestId = msgEvent.shouldHaveRequestId()

        // Verify request params
        msgEvent.data[FIELD_START]?.jsonPrimitive?.int shouldBe 0
        msgEvent.data[FIELD_END]?.jsonPrimitive?.int shouldBe 2

        // 4. Send response to simulate frontend
        sendResponse(
            commId,
            requestId,
            status = "ok",
            result =
                buildResult {
                    put(FIELD_CELLS, buildJsonArray {})
                },
        )

        // 5. Wait for completion - this verifies the response was processed
        awaitAsyncResult<Any?>(resultVar)
    }

    @Test
    fun `should send splice_cell_range with deleteCount when deleting cell`() {
        // 1. Start async operation (runs in thread INSIDE REPL)
        val resultVar = runNotekit("deleteCell(2)")

        // 2. Verify comm events in order
        val (commId) = shouldHaveNextOpenEvent(NOTEKIT_PROTOCOL_TARGET)

        val msgEvent =
            shouldHaveNextMessageEvent()
                .shouldHaveMethod(METHOD_SPLICE_CELL_RANGE)
        val requestId = msgEvent.shouldHaveRequestId()

        msgEvent.data[FIELD_START]?.jsonPrimitive?.int shouldBe 2
        msgEvent.data[FIELD_DELETE_COUNT]?.jsonPrimitive?.int shouldBe 1

        // 4. Send response to unblock the operation
        sendResponse(
            commId,
            requestId,
            status = STATUS_OK,
            result = buildResult {},
        )

        // 5. Wait for completion
        awaitAsyncResult<Any?>(resultVar)
    }

    @Test
    fun `should send correct setNotebookMetadata request with merge param`() {
        // 1. Start async operation (runs in thread INSIDE REPL)
        execSuccess(
            """
            import kotlinx.serialization.json.JsonPrimitive
            import kotlinx.serialization.json.buildJsonObject
            """.trimIndent(),
        )

        val resultVar =
            runNotekit(
                """
                setNotebookMetadata(buildJsonObject{ put("key", JsonPrimitive("value")) }, merge = true)
                """.trimIndent(),
            )

        // 2. Verify comm events in order
        val (commId) = shouldHaveNextOpenEvent(NOTEKIT_PROTOCOL_TARGET)

        val msgEvent =
            shouldHaveNextMessageEvent()
                .shouldHaveMethod(METHOD_SET_NOTEBOOK_METADATA)
        val requestId = msgEvent.shouldHaveRequestId()

        // Verify metadata and merge params exist
        msgEvent.data[FIELD_METADATA].shouldNotBeNull()
        msgEvent.data[FIELD_MERGE].shouldNotBeNull()

        // 4. Send response to unblock the operation
        sendResponse(
            commId,
            requestId,
            status = STATUS_OK,
            result = buildResult {},
        )

        // 5. Wait for completion
        awaitAsyncResult<Any?>(resultVar)
    }

    @Test
    fun `should open comm with correct target name`() {
        // 1. Start async operation (runs in thread INSIDE REPL)
        val resultVar = runNotekit("getCellCount()")

        // 2. Verify comm was opened with correct target
        val (commId) = shouldHaveNextOpenEvent(NOTEKIT_PROTOCOL_TARGET)

        // 4. Send response to complete the operation
        val requestId = shouldHaveNextMessageEvent().shouldHaveRequestId()
        sendResponse(
            commId,
            requestId,
            status = STATUS_OK,
            result =
                buildResult {
                    put(FIELD_COUNT, 5)
                },
        )

        // 5. Wait for completion and verify result
        val result = awaitAsyncResult<Int>(resultVar)
        result shouldBe 5
    }
}
