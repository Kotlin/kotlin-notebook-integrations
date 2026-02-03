package org.jetbrains.jupyter.parser.tests

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import org.jetbrains.jupyter.parser.notebook.ExecuteTime
import org.jetbrains.jupyter.parser.notebook.Execution
import org.junit.jupiter.api.Test
import java.time.Instant

class ExecuteTimeTests {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test ExecuteTime serialization`() {
        val startTime = Instant.parse("2025-12-04T11:17:27.571008Z")
        val endTime = Instant.parse("2025-12-04T11:17:29.197564Z")
        val executeTime = ExecuteTime(startTime, endTime)

        json.encodeToString<ExecuteTime>(executeTime) shouldBe
            """{"start_time":"2025-12-04T11:17:27.571008Z","end_time":"2025-12-04T11:17:29.197564Z"}"""
    }

    @Test
    fun `test ExecuteTime deserialization`() {
        val jsonString =
            """{"start_time":"2025-12-04T11:17:27.571008Z","end_time":"2025-12-04T11:17:29.197564Z"}"""
        val executeTime = json.decodeFromString<ExecuteTime>(jsonString)

        executeTime.startTime shouldBe Instant.parse("2025-12-04T11:17:27.571008Z")
        executeTime.endTime shouldBe Instant.parse("2025-12-04T11:17:29.197564Z")
    }

    @Test
    fun `test ExecuteTime deserialization with null values`() {
        val executeTime = json.decodeFromString<ExecuteTime>("""{}""")

        executeTime.startTime shouldBe null
        executeTime.endTime shouldBe null
    }

    @Test
    fun `test ExecuteTime serialization with null values`() {
        val executeTime = ExecuteTime(null, null)

        json.encodeToString<ExecuteTime>(executeTime) shouldBe """{}"""
    }

    @Test
    fun `test ExecuteTime with only start time`() {
        val startTime = Instant.parse("2025-12-04T11:17:27.571008Z")
        val executeTime = ExecuteTime(startTime, null)

        val serialized = json.encodeToString(executeTime)
        val deserialized = json.decodeFromString<ExecuteTime>(serialized)

        deserialized.startTime shouldBe startTime
        deserialized.endTime shouldBe null
    }

    @Test
    fun `test Execution serialization`() {
        val executeInput = Instant.parse("2025-12-04T11:17:27.000000Z")
        val statusBusy = Instant.parse("2025-12-04T11:17:27.100000Z")
        val statusIdle = Instant.parse("2025-12-04T11:17:29.200000Z")
        val executeReply = Instant.parse("2025-12-04T11:17:29.300000Z")
        val execution =
            Execution(
                iopubExecuteInput = executeInput,
                iopubStatusBusy = statusBusy,
                iopubStatusIdle = statusIdle,
                shellExecuteReply = executeReply,
            )

        json.encodeToString<Execution>(execution) shouldBe
            """{"iopub.execute_input":"2025-12-04T11:17:27Z","iopub.status.busy":"2025-12-04T11:17:27.100Z","iopub.status.idle":"2025-12-04T11:17:29.200Z","shell.execute_reply":"2025-12-04T11:17:29.300Z"}"""
    }

    @Test
    fun `test Execution deserialization`() {
        val jsonString =
            """{"iopub.execute_input":"2025-12-04T11:17:27Z","iopub.status.busy":"2025-12-04T11:17:27.100Z","iopub.status.idle":"2025-12-04T11:17:29.200Z","shell.execute_reply":"2025-12-04T11:17:29.300Z"}"""
        val execution = json.decodeFromString<Execution>(jsonString)

        execution.iopubExecuteInput shouldBe Instant.parse("2025-12-04T11:17:27.000000Z")
        execution.iopubStatusBusy shouldBe Instant.parse("2025-12-04T11:17:27.100000Z")
        execution.iopubStatusIdle shouldBe Instant.parse("2025-12-04T11:17:29.200000Z")
        execution.shellExecuteReply shouldBe Instant.parse("2025-12-04T11:17:29.300000Z")
    }

    @Test
    fun `test Execution with partial fields`() {
        val jsonString =
            """{"iopub.execute_input":"2025-12-04T11:17:27Z","iopub.status.idle":"2025-12-04T11:17:29.200Z"}"""
        val execution = json.decodeFromString<Execution>(jsonString)

        execution.iopubExecuteInput shouldBe Instant.parse("2025-12-04T11:17:27Z")
        execution.iopubStatusBusy shouldBe null
        execution.iopubStatusIdle shouldBe Instant.parse("2025-12-04T11:17:29.200Z")
        execution.shellExecuteReply shouldBe null
    }
}
