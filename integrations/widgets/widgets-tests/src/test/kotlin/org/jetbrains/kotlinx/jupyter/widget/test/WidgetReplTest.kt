package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
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
    }

    @Test
    fun `nested widget property change sends comm message`() {
        execRaw("val s2 = intSliderWidget()")
        execRaw("s2.layout.width = \"100px\"")

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

    companion object {
        private val facility = TestServerCommCommunicationFacility()
        private val provider = WidgetReplProvider(CommManagerImpl(facility))
    }
}
