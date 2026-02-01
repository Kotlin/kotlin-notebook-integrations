package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test

class ButtonWidgetTest : AbstractWidgetReplTest() {
    @Test
    fun `button onClick should be triggered by frontend click event`() {
        execRaw("val b = buttonWidget { description = \"Click me\" }")
        val buttonId = execRaw("widgetManager.getWidgetId(b)") as String

        execRaw("var clicked = false")
        execRaw("b.onClick { clicked = true }")

        val clickEvent =
            buildJsonObject {
                put("method", "custom")
                put(
                    "content",
                    buildJsonObject {
                        put("event", "click")
                    },
                )
            }
        commManager.processCommMessage(buttonId, clickEvent, null, emptyList())

        execRaw("clicked") shouldBe true
    }
}
