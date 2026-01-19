package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.jetbrains.kotlinx.jupyter.protocol.comms.CommManagerImpl
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import org.jetbrains.kotlinx.jupyter.widget.test.util.CommEvent
import org.jetbrains.kotlinx.jupyter.widget.test.util.TestServerCommCommunicationFacility
import org.jetbrains.kotlinx.jupyter.widget.test.util.WidgetReplProvider

abstract class AbstractWidgetReplTest(
    protected val facility: TestServerCommCommunicationFacility = TestServerCommCommunicationFacility(),
    protected val commManager: CommManagerImpl = CommManagerImpl(facility),
    provider: WidgetReplProvider = WidgetReplProvider(commManager),
) : JupyterReplTestCase(provider) {
    protected var nextEventIndex: Int = 0

    protected fun resetEvents() {
        facility.sentEvents.clear()
        nextEventIndex = 0
    }

    protected fun shouldHaveOpenEvents(vararg expectedModelNames: String): List<CommEvent.Open> =
        expectedModelNames.map { shouldHaveNextOpenEvent(it) }

    protected fun shouldHaveNextOpenEvent(expectedModelName: String) = shouldHaveOpenEvent(nextEventIndex++, expectedModelName)

    protected fun shouldHaveOpenEvent(
        index: Int,
        expectedModelName: String,
    ): CommEvent.Open {
        val openEvent = facility.sentEvents[index].shouldBeInstanceOf<CommEvent.Open>()
        openEvent.data["state"]
            ?.shouldBeInstanceOf<JsonObject>()
            ?.get("_model_name")
            ?.jsonPrimitive
            ?.content shouldBe expectedModelName
        return openEvent
    }

    protected fun shouldHaveNextUpdateEvent(vararg expectedState: Pair<String, Any?>) =
        shouldHaveMessageEvent(nextEventIndex++, "update", *expectedState)

    protected fun shouldHaveNextEchoUpdateEvent(vararg expectedState: Pair<String, Any?>) =
        shouldHaveMessageEvent(nextEventIndex++, "echo_update", *expectedState)

    protected fun shouldHaveMessageEvent(
        index: Int,
        method: String,
        vararg expectedState: Pair<String, Any?>,
    ): CommEvent.Message {
        val msgEvent = facility.sentEvents[index].shouldBeInstanceOf<CommEvent.Message>()
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

    protected fun shouldHaveUpdateEvent(
        propertyName: String,
        vararg expectedState: Pair<String, Any?>,
    ): CommEvent.Message {
        val event =
            facility.sentEvents.filterIsInstance<CommEvent.Message>().find {
                it.data["method"]?.jsonPrimitive?.content == "update" &&
                    it.data["state"]?.shouldBeInstanceOf<JsonObject>()?.containsKey(propertyName) == true
            }
        event.shouldNotBeNull()
        return event.shouldHaveMessage("update", *expectedState)
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
        commManager.processCommMessage(commId, updateData, null, buffers)
    }

    protected fun buildState(vararg state: Pair<String, Any?>) = buildJsonObject { putState(*state) }

    protected fun JsonObjectBuilder.putState(vararg state: Pair<String, Any?>) {
        for ((key, value) in state) {
            when (value) {
                is String -> put(key, value)
                is Number -> put(key, value)
                is Boolean -> put(key, value)
                is JsonElement -> put(key, value)
                null -> put(key, null as String?)
                else -> throw IllegalArgumentException("Unsupported type: ${value::class}")
            }
        }
    }
}
