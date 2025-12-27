package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test

class CustomMessageTest : AbstractWidgetReplTest() {
    @Test
    fun `send custom message from kernel`() {
        execRaw("val s = intSliderWidget()")
        // LayoutModel, SliderStyleModel, IntSliderModel
        val sliderId = facility.sentEvents[2].shouldBeInstanceOf<CommEvent.Open>().commId
        nextEventIndex = 3

        execRaw("import kotlinx.serialization.json.buildJsonObject")
        execRaw("import kotlinx.serialization.json.put")
        execRaw("s.sendCustomMessage(buildJsonObject { put(\"foo\", \"bar\") })")

        val msgEvent = facility.sentEvents[nextEventIndex++].shouldBeInstanceOf<CommEvent.Message>()
        msgEvent.commId shouldBe sliderId
        msgEvent.data["method"]?.shouldBeInstanceOf<JsonPrimitive>()?.content shouldBe "custom"
        val content = msgEvent.data["content"]?.shouldBeInstanceOf<kotlinx.serialization.json.JsonObject>()!!
        content["foo"]?.shouldBeInstanceOf<JsonPrimitive>()?.content shouldBe "bar"
    }

    @Test
    fun `send custom message with bytes from kernel`() {
        execRaw("val s = intSliderWidget()")
        val sliderId = facility.sentEvents[2].shouldBeInstanceOf<CommEvent.Open>().commId
        nextEventIndex = 3

        execRaw("import kotlinx.serialization.json.buildJsonObject")
        execRaw("import kotlinx.serialization.json.put")
        execRaw("s.sendCustomMessage(buildJsonObject { put(\"data\", null as String?) }, buffers = listOf(byteArrayOf(1, 2, 3)))")

        val msgEvent = facility.sentEvents[nextEventIndex++].shouldBeInstanceOf<CommEvent.Message>()
        msgEvent.commId shouldBe sliderId
        msgEvent.data["method"]?.shouldBeInstanceOf<JsonPrimitive>()?.content shouldBe "custom"
        msgEvent.buffers shouldBe listOf(byteArrayOf(1, 2, 3))
    }

    @Test
    fun `receive custom message from frontend`() {
        execRaw("val s = intSliderWidget()")
        val sliderId = facility.sentEvents[2].shouldBeInstanceOf<CommEvent.Open>().commId

        execRaw("var receivedContent: kotlinx.serialization.json.JsonObject? = null")
        execRaw("s.addCustomMessageListener { content, _, _ -> receivedContent = content }")

        val content = buildJsonObject { put("baz", 42) }
        val customData =
            buildJsonObject {
                put("method", "custom")
                put("content", content)
            }
        commManager.processCommMessage(sliderId, customData, null, emptyList())

        execRaw("receivedContent?.get(\"baz\")?.toString()") shouldBe "42"
    }

    @Test
    fun `receive custom message with bytes from frontend`() {
        execRaw("val s = intSliderWidget()")
        val sliderId = facility.sentEvents[2].shouldBeInstanceOf<CommEvent.Open>().commId

        execRaw("var receivedData: ByteArray? = null")
        execRaw("s.addCustomMessageListener { _, _, buffers -> receivedData = buffers.firstOrNull() }")

        val bytes = byteArrayOf(4, 5, 6)
        val content = buildJsonObject { put("data", null as String?) }
        val customData =
            buildJsonObject {
                put("method", "custom")
                put("content", content)
            }
        commManager.processCommMessage(sliderId, customData, null, listOf(bytes))

        execRaw("receivedData") shouldBe bytes
    }

    @Test
    fun `receive custom message with metadata from frontend`() {
        execRaw("val s = intSliderWidget()")
        val sliderId = facility.sentEvents[2].shouldBeInstanceOf<CommEvent.Open>().commId

        execRaw("var receivedMetadata: kotlinx.serialization.json.JsonElement? = null")
        execRaw("s.addCustomMessageListener { _, metadata, _ -> receivedMetadata = metadata }")

        val content = buildJsonObject { put("foo", "bar") }
        val metadata = buildJsonObject { put("meta", "data") }
        val customData =
            buildJsonObject {
                put("method", "custom")
                put("content", content)
            }
        commManager.processCommMessage(sliderId, customData, metadata, emptyList())

        execRaw("import kotlinx.serialization.json.jsonPrimitive")
        execRaw("import kotlinx.serialization.json.jsonObject")
        execRaw("(receivedMetadata as kotlinx.serialization.json.JsonObject).get(\"meta\")?.jsonPrimitive?.content") shouldBe "data"
    }

    @Test
    fun `receive custom message with primitive metadata from frontend`() {
        execRaw("val s = intSliderWidget()")
        val sliderId = facility.sentEvents[2].shouldBeInstanceOf<CommEvent.Open>().commId

        execRaw("var receivedMetadata: kotlinx.serialization.json.JsonElement? = null")
        execRaw("s.addCustomMessageListener { _, metadata, _ -> receivedMetadata = metadata }")

        val content = buildJsonObject { put("foo", "bar") }
        val metadata = JsonPrimitive("some string")
        val customData =
            buildJsonObject {
                put("method", "custom")
                put("content", content)
            }
        commManager.processCommMessage(sliderId, customData, metadata, emptyList())

        execRaw("import kotlinx.serialization.json.jsonPrimitive")
        execRaw("receivedMetadata?.jsonPrimitive?.content") shouldBe "some string"
    }

    @Test
    fun `send custom message with primitive metadata from kernel`() {
        execRaw("val s = intSliderWidget()")
        val sliderId = facility.sentEvents[2].shouldBeInstanceOf<CommEvent.Open>().commId
        nextEventIndex = 3

        execRaw("import kotlinx.serialization.json.buildJsonObject")
        execRaw("import kotlinx.serialization.json.put")
        execRaw("import kotlinx.serialization.json.JsonPrimitive")
        execRaw("s.sendCustomMessage(buildJsonObject { put(\"foo\", \"bar\") }, metadata = JsonPrimitive(123))")

        val msgEvent = facility.sentEvents[nextEventIndex++].shouldBeInstanceOf<CommEvent.Message>()
        msgEvent.commId shouldBe sliderId
        msgEvent.data["method"]?.shouldBeInstanceOf<JsonPrimitive>()?.content shouldBe "custom"
        msgEvent.metadata?.shouldBeInstanceOf<JsonPrimitive>()?.content shouldBe "123"
    }

    @Test
    fun `send raw custom message with bytes from kernel`() {
        execRaw("val s = intSliderWidget()")
        val sliderId = facility.sentEvents[2].shouldBeInstanceOf<CommEvent.Open>().commId
        nextEventIndex = 3

        execRaw("import kotlinx.serialization.json.buildJsonObject")
        execRaw("s.sendCustomMessage(buildJsonObject {}, buffers = listOf(byteArrayOf(7, 8, 9)))")

        val msgEvent = facility.sentEvents[nextEventIndex++].shouldBeInstanceOf<CommEvent.Message>()
        msgEvent.commId shouldBe sliderId
        msgEvent.buffers shouldBe listOf(byteArrayOf(7, 8, 9))
    }
}
