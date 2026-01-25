package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.kotlinx.jupyter.messaging.makeRawMessage
import org.jetbrains.kotlinx.jupyter.protocol.api.EMPTY
import org.jetbrains.kotlinx.jupyter.protocol.api.RawMessage
import org.jetbrains.kotlinx.jupyter.widget.display.WidgetDisplayController
import org.jetbrains.kotlinx.jupyter.widget.library.OutputWidget
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel
import org.junit.jupiter.api.Test

class OutputWidgetTest {
    private class MockDisplayController : WidgetDisplayController {
        override var contextMessage: RawMessage? = null
        var onClearOutput: ((wait: Boolean) -> Unit)? = null

        override fun clearOutput(wait: Boolean) {
            onClearOutput?.invoke(wait)
        }
    }

    private class MockWidgetManager(
        override val displayController: MockDisplayController,
    ) : TestWidgetManager {
        override fun getWidgetId(widget: WidgetModel): String = "test-id"
    }

    private val displayController = MockDisplayController()
    private val widgetManager = MockWidgetManager(displayController)

    private fun createRawMessage(messageId: String): RawMessage =
        makeRawMessage(
            zmqIdentities = emptyList(),
            dataJson =
                buildJsonObject {
                    put(
                        "header",
                        buildJsonObject {
                            put("msg_id", messageId)
                        },
                    )
                    put("content", Json.EMPTY)
                },
        )

    @Test
    fun `clearOutput should call displayController and use scope`() {
        val widget = OutputWidget(widgetManager, false)
        val testId = "msg-123"
        displayController.contextMessage = createRawMessage(testId)

        var msgIdDuringClear: String? = null
        displayController.onClearOutput = { wait ->
            wait shouldBe true
            msgIdDuringClear = widget.msgId
        }

        widget.clearOutput(wait = true)
        msgIdDuringClear shouldBe testId
        widget.msgId shouldBe "" // reset after withScope
    }

    @Test
    fun `withScope should set msgId from contextMessage`() {
        val widget = OutputWidget(widgetManager, false)
        val testId = "msg-456"
        displayController.contextMessage = createRawMessage(testId)

        widget.msgId shouldBe ""

        widget.withScope {
            widget.msgId shouldBe testId
        }

        widget.msgId shouldBe ""
    }

    @Test
    fun `withScope should handle nested calls`() {
        val widget = OutputWidget(widgetManager, false)
        val testId1 = "msg-1"
        val testId2 = "msg-2"

        displayController.contextMessage = createRawMessage(testId1)
        widget.withScope {
            widget.msgId shouldBe testId1

            displayController.contextMessage = createRawMessage(testId2)
            widget.withScope {
                widget.msgId shouldBe testId2
            }

            widget.msgId shouldBe testId2 // After inner scope, it's NOT reset yet
        }
        widget.msgId shouldBe ""
    }
}
