package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.jetbrains.kotlinx.jupyter.protocol.comms.CommManagerImpl
import org.jetbrains.kotlinx.jupyter.test.util.AbstractCommReplTest
import org.jetbrains.kotlinx.jupyter.test.util.CommEvent
import org.jetbrains.kotlinx.jupyter.test.util.IntegrationReplProvider
import org.jetbrains.kotlinx.jupyter.test.util.TestServerCommCommunicationFacility

abstract class AbstractWidgetReplTest(
    facility: TestServerCommCommunicationFacility = TestServerCommCommunicationFacility(),
    commManager: CommManagerImpl = CommManagerImpl(facility),
    provider: IntegrationReplProvider = IntegrationReplProvider(commManager, "widgets"),
) : AbstractCommReplTest(facility, commManager, provider) {
    protected fun shouldHaveOpenEvents(vararg expectedModelNames: String): List<CommEvent.Open> =
        expectedModelNames.map { shouldHaveNextWidgetOpenEvent(it) }

    protected fun shouldHaveNextWidgetOpenEvent(expectedModelName: String): CommEvent.Open =
        facility.getNextEvent().shouldBeWidgetOpen(expectedModelName)

    protected fun CommEvent.shouldBeWidgetOpen(expectedModelName: String): CommEvent.Open {
        val openEvent = this.shouldBeInstanceOf<CommEvent.Open>()
        openEvent.data["state"]
            ?.shouldBeInstanceOf<JsonObject>()
            ?.get("_model_name")
            ?.jsonPrimitive
            ?.content shouldBe expectedModelName
        return openEvent
    }

    protected fun shouldHaveNextUpdateEvent(vararg expectedState: Pair<String, Any?>) =
        facility.getNextEvent().shouldBeMessage("update", *expectedState)

    protected fun shouldHaveNextEchoUpdateEvent(vararg expectedState: Pair<String, Any?>) =
        facility.getNextEvent().shouldBeMessage("echo_update", *expectedState)

    protected fun CommEvent.shouldBeMessage(
        method: String,
        vararg expectedState: Pair<String, Any?>,
    ): CommEvent.Message {
        val msgEvent = this.shouldBeInstanceOf<CommEvent.Message>()
        return msgEvent.shouldHaveMessage(method, *expectedState)
    }

    protected fun CommEvent.Message.shouldHaveMessage(
        method: String,
        vararg expectedState: Pair<String, Any?>,
    ): CommEvent.Message {
        data["method"]?.jsonPrimitive?.content shouldBe method
        val state = data["state"].shouldBeInstanceOf<JsonObject>()
        for ((key, value) in expectedState) {
            state[key]?.jsonPrimitive?.content shouldBe value?.toString()
        }
        return this
    }

    protected fun shouldHaveBufferPath(
        msgEvent: CommEvent.Message,
        index: Int,
        vararg expectedPath: String,
    ) {
        val paths = msgEvent.data["buffer_paths"]?.shouldBeInstanceOf<JsonArray>()!!
        val path = paths[index].shouldBeInstanceOf<JsonArray>()
        path.size shouldBe expectedPath.size
        expectedPath.forEachIndexed { i, segment ->
            path[i].jsonPrimitive.content shouldBe segment
        }
    }

    protected fun shouldHaveWidgetDisplayJson(
        json: JsonObject?,
        expectedModelId: String,
        expectedModelName: String,
    ) {
        json.shouldNotBeNull()
        val data = json["data"].shouldBeInstanceOf<JsonObject>()
        val viewData = data["application/vnd.jupyter.widget-view+json"].shouldBeInstanceOf<JsonObject>()
        viewData["model_id"]?.jsonPrimitive?.content shouldBe expectedModelId
        viewData["version_major"]?.jsonPrimitive?.content shouldBe "2"
        viewData["version_minor"]?.jsonPrimitive?.content shouldBe "0"

        val htmlData = data["text/html"].shouldBeInstanceOf<JsonPrimitive>().content
        htmlData shouldBe "$expectedModelName(id=$expectedModelId)"
    }

    protected fun sendUpdate(
        commId: String,
        vararg state: Pair<String, Any?>,
    ) = sendUpdate(commId, buildState(*state))

    protected fun sendUpdate(
        commId: String,
        state: JsonObject,
        buffers: List<ByteArray> = emptyList(),
        bufferPaths: List<List<String>> = emptyList(),
    ) {
        val updateData =
            buildJsonObject {
                put("method", "update")
                put("state", state)
                put(
                    "buffer_paths",
                    buildJsonArray {
                        bufferPaths.forEach { path ->
                            add(buildJsonArray { path.forEach { add(it) } })
                        }
                    },
                )
            }
        sendCommMessage(commId, updateData, buffers)
    }
}
