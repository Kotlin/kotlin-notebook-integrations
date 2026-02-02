package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.kotlinx.jupyter.widget.library.DirectionalLinkWidget
import org.jetbrains.kotlinx.jupyter.widget.library.LinkWidget
import org.junit.jupiter.api.Test

class LinkWidgetTest : AbstractWidgetReplTest() {
    @Test
    fun `linkProperties should create a LinkWidget with correct source and target`() {
        execRaw("val play = playWidget()")
        execRaw("val slider = intSliderWidget()")

        val playId = execRaw("widgetManager.getWidgetId(play)") as String
        val sliderId = execRaw("widgetManager.getWidgetId(slider)") as String

        val linkingCodes =
            listOf(
                """widgetManager.linkProperties(play, PlayWidget::value, slider, IntSliderWidget::value)""",
                """widgetManager.linkProperties(play, { ::value }, slider, { ::value })""",
            )

        for (code in linkingCodes) {
            resetEvents()
            val link = execRaw(code)
            link.shouldBeInstanceOf<LinkWidget>()

            facility.getNextEvent().shouldBeWidgetOpen("LinkModel")

            val source =
                facility
                    .getNextEvent()
                    .shouldBeMessage("update")
                    .data["state"]!!
                    .shouldBeInstanceOf<JsonObject>()["source"]
                    .shouldBeInstanceOf<JsonArray>()
            source[0].jsonPrimitive.contentOrNull shouldBe "IPY_MODEL_$playId"
            source[1].jsonPrimitive.content shouldBe "value"

            val target =
                facility
                    .getNextEvent()
                    .shouldBeMessage("update")
                    .data["state"]!!
                    .shouldBeInstanceOf<JsonObject>()["target"]
                    .shouldBeInstanceOf<JsonArray>()
            target[0].jsonPrimitive.contentOrNull shouldBe "IPY_MODEL_$sliderId"
            target[1].jsonPrimitive.content shouldBe "value"
        }
    }

    @Test
    fun `linkPropertiesOneWay should create a DirectionalLinkWidget with correct source and target`() {
        execRaw("val play = playWidget()")
        execRaw("val slider = intSliderWidget()")
        resetEvents()

        val playId = execRaw("widgetManager.getWidgetId(play)") as String
        val sliderId = execRaw("widgetManager.getWidgetId(slider)") as String

        val link =
            execRaw(
                """
                widgetManager.linkPropertiesOneWay(play, PlayWidget::value, slider, IntSliderWidget::value)
                """.trimIndent(),
            )
        link.shouldBeInstanceOf<DirectionalLinkWidget>()

        facility.getNextEvent().shouldBeWidgetOpen("DirectionalLinkModel")

        val source =
            facility
                .getNextEvent()
                .shouldBeMessage("update")
                .data["state"]!!
                .shouldBeInstanceOf<JsonObject>()["source"]
                .shouldBeInstanceOf<JsonArray>()
        source[0].jsonPrimitive.contentOrNull shouldBe "IPY_MODEL_$playId"
        source[1].jsonPrimitive.content shouldBe "value"

        val target =
            facility
                .getNextEvent()
                .shouldBeMessage("update")
                .data["state"]!!
                .shouldBeInstanceOf<JsonObject>()["target"]
                .shouldBeInstanceOf<JsonArray>()
        target[0].jsonPrimitive.contentOrNull shouldBe "IPY_MODEL_$sliderId"
        target[1].jsonPrimitive.content shouldBe "value"
    }
}
