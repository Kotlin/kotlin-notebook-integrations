package org.jetbrains.kotlinx.jupyter.notebook.test

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlinx.jupyter.notebook.integration.NotekitResult
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_ERROR
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_METHOD
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_REQUEST_ID
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_RESULT
import org.jetbrains.kotlinx.jupyter.notebook.protocol.FIELD_STATUS
import org.jetbrains.kotlinx.jupyter.notebook.protocol.STATUS_OK
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
        data[FIELD_METHOD]?.jsonPrimitive?.content shouldBe expectedMethod
        return this
    }

    protected fun CommEvent.Message.shouldHaveRequestId(): String {
        val requestId = data[FIELD_REQUEST_ID]?.jsonPrimitive?.content
        requestId.shouldNotBeNull()
        return requestId
    }

    protected fun sendResponse(
        commId: String,
        requestId: String,
        status: String = STATUS_OK,
        result: JsonObject? = null,
        error: JsonObject? = null,
    ) {
        val responseData =
            buildJsonObject {
                put(FIELD_REQUEST_ID, requestId)
                put(FIELD_STATUS, status)
                if (result != null) {
                    put(FIELD_RESULT, result)
                }
                if (error != null) {
                    put(FIELD_ERROR, error)
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
    protected inline fun <reified T> awaitAsyncResult(result: NotekitResult<*>): T =
        runBlocking {
            result.asyncResult.await() as T
        }
}
