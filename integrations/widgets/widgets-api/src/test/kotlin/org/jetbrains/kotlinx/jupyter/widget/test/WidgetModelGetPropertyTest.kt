package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlinx.jupyter.widget.library.AccordionWidget
import org.jetbrains.kotlinx.jupyter.widget.model.getProperty
import org.junit.jupiter.api.Test

class WidgetModelGetPropertyTest {
    private val widgetManager = TestWidgetManager.INSTANCE

    @Test
    fun `getProperty with KProperty1 should return the property`() {
        val accordion = AccordionWidget(widgetManager, false)
        val property = accordion.getProperty(AccordionWidget::selectedIndex)

        property.shouldNotBeNull()
        property.name shouldBe "selected_index"
    }

    @Test
    fun `getProperty with KProperty0 should return the property`() {
        val accordion = AccordionWidget(widgetManager, false)
        val property = accordion.getProperty(accordion::selectedIndex)

        property.shouldNotBeNull()
        property.name shouldBe "selected_index"
    }

    @Test
    fun `getProperty with String should return the property`() {
        val accordion = AccordionWidget(widgetManager, false)
        val property = accordion.getProperty("selected_index")

        property.shouldNotBeNull()
        property.name shouldBe "selected_index"
    }
}
