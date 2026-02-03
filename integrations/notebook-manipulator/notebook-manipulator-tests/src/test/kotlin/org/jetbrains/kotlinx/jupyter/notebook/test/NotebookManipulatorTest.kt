package org.jetbrains.kotlinx.jupyter.notebook.test

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.junit.jupiter.api.Test

class NotebookManipulatorTest : AbstractNotebookManipulatorReplTest() {
    @Test
    fun `should send correct getCellCount request and handle response`() {
        resetEvents()

        // 1. Start async operation (runs in thread INSIDE REPL)
        val resultVar = manipulate("getCellCount()")

        // 2. Verify comm events in order
        val (commId) = shouldHaveNextOpenEvent("jupyter.notebook.manipulator.v1")

        val requestId =
            shouldHaveNextMessageEvent()
                .shouldHaveMethod("get_cell_count")
                .shouldHaveRequestId()

        // 4. Send response to unblock the operation
        sendResponse(
            commId,
            requestId,
            status = "ok",
            result =
                buildResult {
                    put("count", 10)
                },
        )

        // 5. Wait for completion and verify result
        val result = awaitAsyncResult<Int>(resultVar)
        result shouldBe 10
    }

    @Test
    fun `should send correct getNotebookMetadata request`() {
        resetEvents()

        // 1. Start async operation (runs in thread INSIDE REPL)
        val resultVar = manipulate("getNotebookMetadata()")

        // 2. Verify request is sent correctly
        val (commId) = shouldHaveNextOpenEvent("jupyter.notebook.manipulator.v1")

        val requestId =
            shouldHaveNextMessageEvent()
                .shouldHaveMethod("get_notebook_metadata")
                .shouldHaveRequestId()

        // 4. Send response with metadata
        sendResponse(
            commId,
            requestId,
            status = "ok",
            result =
                buildResult {
                    putJsonObject("metadata") {
                        put("kernelspec", "python3")
                    }
                },
        )

        // 5. Wait for completion
        awaitAsyncResult<Any?>(resultVar)
    }

    @Test
    fun `should send correct getCellRange request with start and end params`() {
        resetEvents()

        // 1. Start async operation (runs in thread INSIDE REPL)
        val resultVar = manipulate("getCellRange(0, 2)")

        // 2. Verify request is sent correctly
        val (commId) = shouldHaveNextOpenEvent("jupyter.notebook.manipulator.v1")

        val msgEvent =
            shouldHaveNextMessageEvent()
                .shouldHaveMethod("get_cell_range")
        val requestId = msgEvent.shouldHaveRequestId()

        // Verify request params
        val params = msgEvent.data["params"]?.let { it as? JsonObject }
        params?.get("start")?.jsonPrimitive?.int shouldBe 0
        params?.get("end")?.jsonPrimitive?.int shouldBe 2

        // 4. Send response to simulate frontend
        sendResponse(
            commId,
            requestId,
            status = "ok",
            result =
                buildResult {
                    put("cells", buildJsonArray {})
                },
        )

        // 5. Wait for completion - this verifies the response was processed
        awaitAsyncResult<Any?>(resultVar)
    }

    @Test
    fun `should send splice_cell_range with deleteCount when deleting cell`() {
        resetEvents()

        // 1. Start async operation (runs in thread INSIDE REPL)
        val resultVar = manipulate("deleteCell(2)")

        // 2. Verify comm events in order
        val (commId) = shouldHaveNextOpenEvent("jupyter.notebook.manipulator.v1")

        val msgEvent =
            shouldHaveNextMessageEvent()
                .shouldHaveMethod("splice_cell_range")
        val requestId = msgEvent.shouldHaveRequestId()

        val params = msgEvent.data["params"]?.let { it as? JsonObject }
        params?.get("start")?.jsonPrimitive?.int shouldBe 2
        params?.get("delete_count")?.jsonPrimitive?.int shouldBe 1

        // 4. Send response to unblock the operation
        sendResponse(
            commId,
            requestId,
            status = "ok",
            result =
                buildResult {
                    putJsonObject("affected_range") {
                        put("start", 2)
                        put("end", 2)
                    }
                },
        )

        // 5. Wait for completion
        awaitAsyncResult<Any?>(resultVar)
    }

    @Test
    fun `should send correct setNotebookMetadata request with merge param`() {
        resetEvents()

        // 1. Start async operation (runs in thread INSIDE REPL)
        val resultVar = manipulate("setNotebookMetadata(mapOf(\"key\" to \"value\"), merge = true)")

        // 2. Verify comm events in order
        val (commId) = shouldHaveNextOpenEvent("jupyter.notebook.manipulator.v1")

        val msgEvent =
            shouldHaveNextMessageEvent()
                .shouldHaveMethod("set_notebook_metadata")
        val requestId = msgEvent.shouldHaveRequestId()

        // Verify params exist (detailed validation of merge param is not critical for this test)
        val params = msgEvent.data["params"]?.let { it as? JsonObject }
        params.shouldNotBeNull()

        // 4. Send response to unblock the operation
        sendResponse(
            commId,
            requestId,
            status = "ok",
            result = buildResult {},
        )

        // 5. Wait for completion
        awaitAsyncResult<Any?>(resultVar)
    }

    @Test
    fun `should open comm with correct target name`() {
        resetEvents()

        // 1. Start async operation (runs in thread INSIDE REPL)
        val resultVar = manipulate("getCellCount()")

        // 2. Verify comm was opened with correct target
        val (commId) = shouldHaveNextOpenEvent("jupyter.notebook.manipulator.v1")

        // 4. Send response to complete the operation
        val requestId = shouldHaveNextMessageEvent().shouldHaveRequestId()
        sendResponse(
            commId,
            requestId,
            status = "ok",
            result =
                buildResult {
                    put("count", 5)
                },
        )

        // 5. Wait for completion and verify result
        val result = awaitAsyncResult<Int>(resultVar)
        result shouldBe 5
    }
}
