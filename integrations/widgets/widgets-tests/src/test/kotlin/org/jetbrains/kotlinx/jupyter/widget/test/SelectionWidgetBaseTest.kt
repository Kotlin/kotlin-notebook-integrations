package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SelectionWidgetBaseTest : AbstractWidgetReplTest() {
    @Test
    fun `options as list of pairs`() {
        execRaw("val d = dropdownWidget()")
        execRaw("d.options = listOf(\"One\" to 1, \"Two\" to 2)")

        execRaw("d.options") shouldBe listOf("One" to 1, "Two" to 2)
        execRaw("d.value = 2")
        execRaw("d.index") shouldBe 1
        execRaw("d.label") shouldBe "Two"
        execRaw("d.value") shouldBe 2

        execRaw("d.label = \"One\"")
        execRaw("d.index") shouldBe 0
        execRaw("d.value") shouldBe 1
    }

    @Test
    fun `options change preserves value`() {
        execRaw("val d = dropdownWidget()")
        execRaw("d.options = listOf(\"One\" to 1, \"Two\" to 2)")
        execRaw("d.value = 2")

        execRaw("d.options = listOf(\"Two\" to 2, \"Three\" to 3)")
        execRaw("d.value") shouldBe 2
        execRaw("d.index") shouldBe 0
        execRaw("d.label") shouldBe "Two"
    }

    @Test
    fun `setting value to null`() {
        execRaw("val d = dropdownWidget()")
        execRaw("d.options = listOf(\"a\" to \"a\", \"b\" to \"b\")")
        execRaw("d.value = \"a\"")
        execRaw("d.value = null")
        execRaw("d.index") shouldBe null
    }
}
