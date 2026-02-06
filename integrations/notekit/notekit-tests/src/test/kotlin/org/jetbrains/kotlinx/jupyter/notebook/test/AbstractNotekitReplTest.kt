package org.jetbrains.kotlinx.jupyter.notebook.test

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlinx.jupyter.notebook.integration.NotekitResult
import org.jetbrains.kotlinx.jupyter.protocol.comms.CommManagerImpl
import org.jetbrains.kotlinx.jupyter.test.util.AbstractCommReplTest
import org.jetbrains.kotlinx.jupyter.test.util.CommEvent
import org.jetbrains.kotlinx.jupyter.test.util.IntegrationReplProvider
import org.jetbrains.kotlinx.jupyter.test.util.TestServerCommCommunicationFacility

abstract class AbstractNotekitReplTest(
    facility: TestServerCommCommunicationFacility = TestServerCommCommunicationFacility(),
    commManager: CommManagerImpl = CommManagerImpl(facility),
    provider: IntegrationReplProvider = IntegrationReplProvider(commManager, "notekit"),
) : AbstractCommReplTest(facility, commManager, provider) {
    override fun shouldHaveNextOpenEvent(expectedTargetName: String): CommEvent.Open {
        val openEvent = super.shouldHaveNextOpenEvent(expectedTargetName)
        openEvent.targetName shouldBe expectedTargetName
        return openEvent
    }

    protected fun CommEvent.Message.shouldHaveMethod(expectedMethod: String): CommEvent.Message {
        data["method"]?.jsonPrimitive?.content shouldBe expectedMethod
        return this
    }

    protected fun CommEvent.Message.shouldHaveRequestId(): String {
        val requestId = data["request_id"]?.jsonPrimitive?.content
        requestId.shouldNotBeNull()
        return requestId
    }

    protected fun sendResponse(
        commId: String,
        requestId: String,
        status: String = "ok",
        result: JsonObject? = null,
        error: JsonObject? = null,
    ) {
        val responseData =
            buildJsonObject {
                put("request_id", requestId)
                put("status", status)
                if (result != null) {
                    put("result", result)
                }
                if (error != null) {
                    put("error", error)
                }
            }
        sendCommMessage(commId, responseData)
    }

    protected fun buildResult(builder: JsonObjectBuilder.() -> Unit): JsonObject = buildJsonObject(builder)

    protected fun runNotekit(
        @Language("kotlin") code: String,
    ): NotekitResult<*> =
        execRaw(
            """
            notekit {
                $code
            }
            """.trimIndent(),
        ) as NotekitResult<*>

    /**
     * Waits for the async operation to complete and returns the result.
     */
    protected inline fun <reified T> awaitAsyncResult(result: NotekitResult<*>): T = result.result as T
}
