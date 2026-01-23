package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import org.jetbrains.kotlinx.jupyter.protocol.api.EMPTY
import org.jetbrains.kotlinx.jupyter.widget.library.TimeWidgetStep
import org.junit.jupiter.api.Test

class WidgetReplTest : AbstractWidgetReplTest() {
    @Test
    fun `widget initialization sends comm open`() {
        execRaw("val s = intSliderWidget()")

        // IntSlider depends on Layout and SliderStyle, so 3 widgets are registered
        shouldHaveOpenEvents("LayoutModel", "SliderStyleModel", "IntSliderModel")
        facility.sentEvents.shouldHaveSize(3)
    }

    @Test
    fun `widget property change sends comm message`() {
        execRaw("val s = intSliderWidget()")
        execRaw("s.value = 42")

        shouldHaveNextOpenEvent("LayoutModel")
        shouldHaveNextOpenEvent("SliderStyleModel")
        shouldHaveNextOpenEvent("IntSliderModel")
        val sliderId = execRaw("widgetManager.getWidgetId(s)") as String
        shouldHaveNextUpdateEvent("value" to 42)
        facility.sentEvents.shouldHaveSize(4)

        val displayedWidget = execSuccess("s").displayValue
        val json = displayedWidget?.toJson(Json.EMPTY, null)
        shouldHaveWidgetDisplayJson(json, sliderId, "IntSliderModel")
    }

    @Test
    fun `nested widget property change sends comm message`() {
        execRaw("val s2 = intSliderWidget()")
        execRaw("s2.layout?.width = \"100px\"")

        val layoutId = execRaw("widgetManager.getWidgetId(s2.layout!!)") as String
        shouldHaveNextOpenEvent("LayoutModel")
        shouldHaveNextOpenEvent("SliderStyleModel")
        shouldHaveNextOpenEvent("IntSliderModel")
        val msgEvent = shouldHaveNextUpdateEvent("width" to "100px")
        msgEvent.commId shouldBe layoutId
        facility.sentEvents.shouldHaveSize(4)
    }

    @Test
    fun `image widget with bytes`() {
        execRaw("val img = imageWidget()")
        execRaw("img.value = byteArrayOf(1, 2, 3)")

        // Image depends on Layout, so 2 widgets registered
        shouldHaveNextOpenEvent("LayoutModel")
        shouldHaveNextOpenEvent("ImageModel")
        val msgEvent = shouldHaveNextUpdateEvent()
        msgEvent.buffers.shouldHaveSize(1)
        msgEvent.buffers[0] shouldBe byteArrayOf(1, 2, 3)
        shouldHaveBufferPath(msgEvent, 0, "value")
        facility.sentEvents.shouldHaveSize(3)
    }

    @Test
    fun `date picker with local date`() {
        execRaw("val dp = datePickerWidget()")
        execRaw("dp.value = LocalDate.of(2023, 1, 1)")

        // DatePicker depends on Layout and DescriptionStyle, so 3 widgets registered
        shouldHaveOpenEvents("LayoutModel", "DescriptionStyleModel", "DatePickerModel")
        shouldHaveNextUpdateEvent("value" to "2023-01-01")
        facility.sentEvents.shouldHaveSize(4)
    }

    @Test
    fun `time widget with integer step`() {
        execRaw("val tw = timeWidget()")
        execRaw("tw.step = TimeWidgetStep.DoubleValue(30.0)")
        shouldHaveOpenEvents("LayoutModel", "DescriptionStyleModel", "TimeModel")
        shouldHaveNextUpdateEvent("step" to 30.0)
    }

    @Test
    fun `frontend message updates widget property`() {
        execRaw("val s = intSliderWidget()")
        val sliderId = execRaw("widgetManager.getWidgetId(s)") as String

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

        execRaw("val createdSlider = widgetManager.getWidget(\"new_slider_id\") as IntSliderWidget")
        execRaw("createdSlider.value") shouldBe 55
    }

    @Test
    fun `frontend message updates image bytes`() {
        execRaw("val img = imageWidget()")
        // Image depends on Layout, so 2 widgets registered. Image is the second one.
        val imageId = execRaw("widgetManager.getWidgetId(img)") as String

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
        val sliderId = execRaw("widgetManager.getWidgetId(s)") as String
        resetEvents()

        sendUpdate(sliderId, "value" to 42)

        facility.sentEvents.shouldBeEmpty()
    }

    @Test
    fun `echo_update can be enabled and filters properties`() {
        execRaw("widgetManager.echoUpdateEnabled = true")
        execRaw("val s = intSliderWidget()")
        val sliderId = execRaw("widgetManager.getWidgetId(s)") as String
        resetEvents()

        // 1. Both properties are echoed
        sendUpdate(sliderId, "value" to 42)
        shouldHaveNextEchoUpdateEvent()
        resetEvents()

        // 2. Disable echo for 'value' property
        execRaw("(s as WidgetModel).getProperty(\"value\")?.echoUpdate = false")

        sendUpdate(sliderId, "value" to 43)

        facility.sentEvents.shouldBeEmpty()

        // 3. Mixed properties: one with echo, one without
        // IntSlider has 'step' property too.
        sendUpdate(sliderId, "value" to 44, "step" to 2)

        facility.sentEvents.shouldHaveSize(1)

        val echoEvent = shouldHaveNextEchoUpdateEvent()
        val echoState = echoEvent.data["state"]?.shouldBeInstanceOf<JsonObject>()!!
        echoState.containsKey("step") shouldBe true
        echoState.containsKey("value") shouldBe false

        // Reset for other tests
        execRaw("widgetManager.echoUpdateEnabled = false")
    }

    @Test
    fun `time widget union type property`() {
        execRaw("val t = timeWidget()")
        shouldHaveOpenEvents("LayoutModel", "DescriptionStyleModel", "TimeModel")

        execRaw("t.step = TimeWidgetStep.DoubleValue(10.5)")
        shouldHaveNextUpdateEvent("step" to 10.5)

        execRaw("t.step = TimeWidgetStep.AnyStep")
        shouldHaveNextUpdateEvent("step" to "any")

        facility.sentEvents.shouldHaveSize(5)
    }

    @Test
    fun `time widget union type property from frontend`() {
        execRaw("val t2 = timeWidget()")
        shouldHaveOpenEvents("LayoutModel", "DescriptionStyleModel", "TimeModel")
        val timeId = execRaw("widgetManager.getWidgetId(t2)") as String

        sendUpdate(timeId, "step" to 42.0)
        execRaw("t2.step").shouldBeInstanceOf<TimeWidgetStep.DoubleValue>().value shouldBe 42.0

        sendUpdate(timeId, "step" to "any")
        execRaw("t2.step") shouldBe TimeWidgetStep.AnyStep
    }

    @Test
    fun `date picker step property`() {
        execRaw("val dp = datePickerWidget()")
        shouldHaveOpenEvents("LayoutModel", "DescriptionStyleModel", "DatePickerModel")

        execRaw("dp.step = DatePickerWidgetStep.IntValue(5)")
        shouldHaveNextUpdateEvent("step" to 5)

        execRaw("dp.step = DatePickerWidgetStep.AnyStep")
        shouldHaveNextUpdateEvent("step" to "any")
    }
}
