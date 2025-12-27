package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.jetbrains.kotlinx.jupyter.protocol.api.EMPTY
import org.jetbrains.kotlinx.jupyter.protocol.comms.CommManagerImpl
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WidgetReplTest : JupyterReplTestCase(provider) {
    @BeforeEach
    fun clearEvents() {
        facility.sentEvents.clear()
    }

    @Test
    fun `widget initialization sends comm open`() {
        execRaw("val s = intSliderWidget()")

        // IntSlider depends on Layout and SliderStyle, so 3 widgets are registered
        facility.sentEvents.shouldHaveSize(3)

        val layoutOpen = facility.sentEvents[0].shouldBeInstanceOf<CommEvent.Open>()
        layoutOpen.data["state"]
            ?.shouldBeInstanceOf<JsonObject>()
            ?.get("_model_name")
            ?.jsonPrimitive
            ?.content shouldBe "LayoutModel"

        val styleOpen = facility.sentEvents[1].shouldBeInstanceOf<CommEvent.Open>()
        styleOpen.data["state"]
            ?.shouldBeInstanceOf<JsonObject>()
            ?.get("_model_name")
            ?.jsonPrimitive
            ?.content shouldBe "SliderStyleModel"

        val sliderOpen = facility.sentEvents[2].shouldBeInstanceOf<CommEvent.Open>()
        sliderOpen.data["state"]
            ?.shouldBeInstanceOf<JsonObject>()
            ?.get("_model_name")
            ?.jsonPrimitive
            ?.content shouldBe "IntSliderModel"
    }

    @Test
    fun `widget property change sends comm message`() {
        execRaw("val s = intSliderWidget()")
        execRaw("s.value = 42")

        facility.sentEvents.shouldHaveSize(4)
        val msgEvent = facility.sentEvents[3].shouldBeInstanceOf<CommEvent.Message>()
        msgEvent.data["method"]?.jsonPrimitive?.content shouldBe "update"
        msgEvent.data["state"]
            ?.shouldBeInstanceOf<JsonObject>()
            ?.get("value")
            ?.jsonPrimitive
            ?.content shouldBe "42"

        val displayedWidget = execSuccess("s").displayValue
        val json = displayedWidget?.toJson(Json.EMPTY, null)
        json.shouldNotBeNull()

        val sliderId = (facility.sentEvents[2] as CommEvent.Open).commId
        val data = json["data"].shouldBeInstanceOf<JsonObject>()
        val viewData = data["application/vnd.jupyter.widget-view+json"].shouldBeInstanceOf<JsonObject>()
        viewData["model_id"]?.jsonPrimitive?.content shouldBe sliderId
        viewData["version_major"]?.jsonPrimitive?.content shouldBe "2"
        viewData["version_minor"]?.jsonPrimitive?.content shouldBe "0"

        val htmlData = data["text/html"].shouldBeInstanceOf<JsonPrimitive>().content
        htmlData shouldBe "IntSliderModel(id=$sliderId)"
    }

    @Test
    fun `nested widget property change sends comm message`() {
        execRaw("val s2 = intSliderWidget()")
        execRaw("s2.layout?.width = \"100px\"")

        facility.sentEvents.shouldHaveSize(4)
        val layoutId = (facility.sentEvents[0] as CommEvent.Open).commId
        val msgEvent = facility.sentEvents[3].shouldBeInstanceOf<CommEvent.Message>()
        msgEvent.commId shouldBe layoutId
        msgEvent.data["state"]
            ?.shouldBeInstanceOf<JsonObject>()
            ?.get("width")
            ?.jsonPrimitive
            ?.content shouldBe "100px"
    }

    @Test
    fun `image widget with bytes`() {
        execRaw("val img = imageWidget()")
        execRaw("img.value = byteArrayOf(1, 2, 3)")

        // Image depends on Layout, so 2 widgets registered
        facility.sentEvents.shouldHaveSize(3)
        val msgEvent = facility.sentEvents[2].shouldBeInstanceOf<CommEvent.Message>()
        // Bytes are currently sent as buffers in the protocol, but let's see how they are rendered in data
        // Actually WidgetManagerImpl.initializeWidget uses getWireMessage which handles buffers.
        msgEvent.buffers.shouldHaveSize(1)
        msgEvent.buffers[0] shouldBe byteArrayOf(1, 2, 3)
        msgEvent.data["buffer_paths"]
            ?.shouldBeInstanceOf<JsonArray>()
            ?.shouldHaveSize(1)
            ?.get(0)
            ?.shouldBeInstanceOf<JsonArray>()
            ?.shouldHaveSize(1)
            ?.get(0)
            ?.jsonPrimitive
            ?.content shouldBe "value"
    }

    @Test
    fun `date picker with local date`() {
        execRaw("import java.time.LocalDate")
        execRaw("val dp = datePickerWidget()")
        execRaw("dp.value = LocalDate.of(2023, 1, 1)")

        // DatePicker depends on Layout and DescriptionStyle, so 3 widgets registered
        facility.sentEvents.shouldHaveSize(4)
        val msgEvent = facility.sentEvents[3].shouldBeInstanceOf<CommEvent.Message>()
        msgEvent.data["state"]
            ?.shouldBeInstanceOf<JsonObject>()
            ?.get("value")
            ?.jsonPrimitive
            ?.content shouldBe "2023-01-01"
    }

    @Test
    fun `frontend message updates widget property`() {
        execRaw("val s = intSliderWidget()")
        val sliderId = (facility.sentEvents[2] as CommEvent.Open).commId

        val updateData =
            buildJsonObject {
                put("method", "update")
                put(
                    "state",
                    buildJsonObject {
                        put("value", 42)
                    },
                )
                put("buffer_paths", JsonArray(emptyList()))
            }

        commManager.processCommMessage(sliderId, updateData, null, emptyList())

        execRaw("s.value") shouldBe 42
    }

    @Test
    fun `frontend comm open creates widget`() {
        val openData =
            buildJsonObject {
                put(
                    "state",
                    buildJsonObject {
                        put("_model_name", "IntSliderModel")
                        put("_model_module", "@jupyter-widgets/controls")
                        put("_model_module_version", "2.0.0")
                        put("value", 55)
                    },
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
        val imageId = (facility.sentEvents[1] as CommEvent.Open).commId

        val bytes = byteArrayOf(10, 20, 30)
        val updateData =
            buildJsonObject {
                put("method", "update")
                put(
                    "state",
                    buildJsonObject {
                        put("value", null as String?)
                    },
                )
                put(
                    "buffer_paths",
                    buildJsonArray {
                        add(
                            buildJsonArray {
                                add("value")
                            },
                        )
                    },
                )
            }

        commManager.processCommMessage(imageId, updateData, null, listOf(bytes))

        execRaw("img.value") shouldBe bytes
    }

    companion object {
        private val facility = TestServerCommCommunicationFacility()
        private val commManager = CommManagerImpl(facility)
        private val provider = WidgetReplProvider(commManager)
    }
}
