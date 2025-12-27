package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json
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
import org.jetbrains.kotlinx.jupyter.protocol.api.EMPTY
import org.jetbrains.kotlinx.jupyter.protocol.comms.CommManagerImpl
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import org.jetbrains.kotlinx.jupyter.widget.library.TimeWidgetStep
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WidgetReplTest : JupyterReplTestCase(provider) {
    private var nextEventIndex = 0

    @BeforeEach
    fun resetEvents() {
        facility.sentEvents.clear()
        nextEventIndex = 0
    }

    @Test
    fun `widget initialization sends comm open`() {
        execRaw("val s = intSliderWidget()")

        // IntSlider depends on Layout and SliderStyle, so 3 widgets are registered
        assertOpenEvents("LayoutModel", "SliderStyleModel", "IntSliderModel")
        facility.sentEvents.shouldHaveSize(3)
    }

    @Test
    fun `widget property change sends comm message`() {
        execRaw("val s = intSliderWidget()")
        execRaw("s.value = 42")

        assertNextOpenEvent("LayoutModel")
        assertNextOpenEvent("SliderStyleModel")
        val sliderId = assertNextOpenEvent("IntSliderModel").commId
        assertNextUpdateEvent("value" to 42)
        facility.sentEvents.shouldHaveSize(4)

        val displayedWidget = execSuccess("s").displayValue
        val json = displayedWidget?.toJson(Json.EMPTY, null)
        assertWidgetDisplayJson(json, sliderId, "IntSliderModel")
    }

    @Test
    fun `nested widget property change sends comm message`() {
        execRaw("val s2 = intSliderWidget()")
        execRaw("s2.layout?.width = \"100px\"")

        val layoutId = assertNextOpenEvent("LayoutModel").commId
        assertNextOpenEvent("SliderStyleModel")
        assertNextOpenEvent("IntSliderModel")
        val msgEvent = assertNextUpdateEvent("width" to "100px")
        msgEvent.commId shouldBe layoutId
        facility.sentEvents.shouldHaveSize(4)
    }

    @Test
    fun `image widget with bytes`() {
        execRaw("val img = imageWidget()")
        execRaw("img.value = byteArrayOf(1, 2, 3)")

        // Image depends on Layout, so 2 widgets registered
        assertNextOpenEvent("LayoutModel")
        assertNextOpenEvent("ImageModel")
        val msgEvent = assertNextUpdateEvent()
        msgEvent.buffers.shouldHaveSize(1)
        msgEvent.buffers[0] shouldBe byteArrayOf(1, 2, 3)
        assertBufferPath(msgEvent, 0, "value")
        facility.sentEvents.shouldHaveSize(3)
    }

    @Test
    fun `date picker with local date`() {
        execRaw("import java.time.LocalDate")
        execRaw("val dp = datePickerWidget()")
        execRaw("dp.value = LocalDate.of(2023, 1, 1)")

        // DatePicker depends on Layout and DescriptionStyle, so 3 widgets registered
        assertOpenEvents("LayoutModel", "DescriptionStyleModel", "DatePickerModel")
        assertNextUpdateEvent("value" to "2023-01-01")
        facility.sentEvents.shouldHaveSize(4)
    }

    @Test
    fun `time widget with integer step`() {
        execRaw("val tw = timeWidget()")
        execRaw("tw.step = TimeWidgetStep.DoubleValue(30.0)")
        assertOpenEvents("LayoutModel", "DescriptionStyleModel", "TimeModel")
        assertNextUpdateEvent("step" to 30.0)
    }

    @Test
    fun `frontend message updates widget property`() {
        execRaw("val s = intSliderWidget()")
        val sliderId = assertOpenEvents("LayoutModel", "SliderStyleModel", "IntSliderModel").last().commId

        sendUpdate(sliderId, "value" to 42)

        execRaw("s.value") shouldBe 42
    }

    @Test
    fun `frontend comm open creates widget`() {
        val openData =
            buildJsonObject {
                put(
                    "state",
                    buildState(
                        "_model_name" to "IntSliderModel",
                        "_model_module" to "@jupyter-widgets/controls",
                        "_model_module_version" to "2.0.0",
                        "value" to 55,
                    ),
                )
                put("buffer_paths", JsonArray(emptyList()))
            }

        commManager.processCommOpen("new_slider_id", "jupyter.widget", openData, null, emptyList())

        // Frontend already has all the widgets, so no events should be sent
        facility.sentEvents.shouldBeEmpty()

        execRaw("import org.jetbrains.kotlinx.jupyter.widget.library.IntSliderWidget")
        execRaw("val createdSlider = widgetManager.getWidget(\"new_slider_id\") as IntSliderWidget")
        execRaw("createdSlider.value") shouldBe 55
    }

    @Test
    fun `frontend message updates image bytes`() {
        execRaw("val img = imageWidget()")
        // Image depends on Layout, so 2 widgets registered. Image is the second one.
        val imageId = assertOpenEvents("LayoutModel", "ImageModel").last().commId

        val bytes = byteArrayOf(10, 20, 30)
        sendUpdate(
            imageId,
            buildState("value" to null),
            listOf(bytes),
            listOf(listOf("value")),
        )

        execRaw("img.value") shouldBe bytes
    }

    @Test
    fun `echo_update is disabled by default`() {
        execRaw("val s = intSliderWidget()")
        val sliderId = assertOpenEvents("LayoutModel", "SliderStyleModel", "IntSliderModel").last().commId
        resetEvents()

        sendUpdate(sliderId, "value" to 42)

        facility.sentEvents.shouldBeEmpty()
    }

    @Test
    fun `echo_update can be enabled and filters properties`() {
        execRaw("widgetManager.echoUpdateEnabled = true")
        execRaw("val s = intSliderWidget()")
        val sliderId = assertOpenEvents("LayoutModel", "SliderStyleModel", "IntSliderModel").last().commId
        resetEvents()

        // 1. Both properties are echoed
        sendUpdate(sliderId, "value" to 42)
        assertNextEchoUpdateEvent()
        resetEvents()

        // 2. Disable echo for 'value' property
        execRaw("import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel")
        execRaw("(s as WidgetModel).getProperty(\"value\")?.echoUpdate = false")

        sendUpdate(sliderId, "value" to 43)

        facility.sentEvents.shouldBeEmpty()

        // 3. Mixed properties: one with echo, one without
        // IntSlider has 'step' property too.
        sendUpdate(sliderId, "value" to 44, "step" to 2)

        facility.sentEvents.shouldHaveSize(1)

        val echoEvent = assertNextEchoUpdateEvent()
        val echoState = echoEvent.data["state"]?.shouldBeInstanceOf<JsonObject>()!!
        echoState.containsKey("step") shouldBe true
        echoState.containsKey("value") shouldBe false

        // Reset for other tests
        execRaw("widgetManager.echoUpdateEnabled = false")
    }

    @Test
    fun `time widget union type property`() {
        execRaw("val t = timeWidget()")
        assertOpenEvents("LayoutModel", "DescriptionStyleModel", "TimeModel")

        execRaw("t.step = TimeWidgetStep.DoubleValue(10.5)")
        assertNextUpdateEvent("step" to 10.5)

        execRaw("t.step = TimeWidgetStep.AnyStep")
        assertNextUpdateEvent("step" to "any")

        facility.sentEvents.shouldHaveSize(5)
    }

    @Test
    fun `time widget union type property from frontend`() {
        execRaw("val t2 = timeWidget()")
        assertOpenEvents("LayoutModel", "DescriptionStyleModel")
        val timeId = assertNextOpenEvent("TimeModel").commId

        sendUpdate(timeId, "step" to 42.0)
        execRaw("t2.step").shouldBeInstanceOf<TimeWidgetStep.DoubleValue>().value shouldBe 42.0

        sendUpdate(timeId, "step" to "any")
        execRaw("t2.step") shouldBe TimeWidgetStep.AnyStep
    }

    @Test
    fun `date picker step property`() {
        execRaw("val dp = datePickerWidget()")
        assertOpenEvents("LayoutModel", "DescriptionStyleModel", "DatePickerModel")

        execRaw("dp.step = DatePickerWidgetStep.IntValue(5)")
        assertNextUpdateEvent("step" to 5)

        execRaw("dp.step = DatePickerWidgetStep.AnyStep")
        assertNextUpdateEvent("step" to "any")
    }

    private fun assertOpenEvents(vararg expectedModelNames: String): List<CommEvent.Open> =
        expectedModelNames.map { assertNextOpenEvent(it) }

    private fun assertNextOpenEvent(expectedModelName: String) = assertOpenEvent(nextEventIndex++, expectedModelName)

    private fun assertOpenEvent(
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

    private fun assertNextUpdateEvent(vararg expectedState: Pair<String, Any?>) =
        assertMessageEvent(nextEventIndex++, "update", *expectedState)

    private fun assertNextEchoUpdateEvent(vararg expectedState: Pair<String, Any?>) =
        assertMessageEvent(nextEventIndex++, "echo_update", *expectedState)

    private fun assertMessageEvent(
        index: Int,
        method: String,
        vararg expectedState: Pair<String, Any?>,
    ): CommEvent.Message {
        val msgEvent = facility.sentEvents[index].shouldBeInstanceOf<CommEvent.Message>()
        msgEvent.data["method"]?.jsonPrimitive?.content shouldBe method
        val state = msgEvent.data["state"].shouldBeInstanceOf<JsonObject>()
        for ((key, value) in expectedState) {
            state[key]?.jsonPrimitive?.content shouldBe value?.toString()
        }
        return msgEvent
    }

    private fun assertBufferPath(
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

    private fun assertWidgetDisplayJson(
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

    private fun sendUpdate(
        commId: String,
        vararg state: Pair<String, Any?>,
    ) = sendUpdate(commId, buildState(*state))

    private fun sendUpdate(
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

    private fun buildState(vararg state: Pair<String, Any?>) = buildJsonObject { putState(*state) }

    private fun JsonObjectBuilder.putState(vararg state: Pair<String, Any?>) {
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

    companion object {
        private val facility = TestServerCommCommunicationFacility()
        private val commManager = CommManagerImpl(facility)
        private val provider = WidgetReplProvider(commManager)
    }
}
