package org.jetbrains.kotlinx.jupyter.widget.generator

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class StringUtilTest {
    @Test
    fun `toPascalCase should handle various delimiters and abbreviations`() {
        "html_style_widget".toPascalCase() shouldBe "HtmlStyleWidget"
        "HTMLStyleWidget".toPascalCase() shouldBe "HtmlStyleWidget"
        "html-style-widget".toPascalCase() shouldBe "HtmlStyleWidget"
        "html style widget".toPascalCase() shouldBe "HtmlStyleWidget"
        "html".toPascalCase() shouldBe "Html"
        "DOM".toPascalCase() shouldBe "Dom"
        "vbox_widget".toPascalCase() shouldBe "VboxWidget"
        "hbox_widget".toPascalCase() shouldBe "HboxWidget"
        "VBoxModel".toPascalCase() shouldBe "VBoxModel"
    }

    @Test
    fun `toCamelCase should handle various delimiters and abbreviations`() {
        "HTMLStyleWidget".toCamelCase() shouldBe "htmlStyleWidget"
        "DOMWidget".toCamelCase() shouldBe "domWidget"
        "vbox".toCamelCase() shouldBe "vbox"
        "hbox_widget".toCamelCase() shouldBe "hboxWidget"
        "VBoxModel".toCamelCase() shouldBe "vBoxModel"
    }

    @Test
    fun `toCamelCase and toPascalCase should be independent`() {
        "html_style_widget".toCamelCase() shouldBe "htmlStyleWidget"
        "html-style-widget".toCamelCase() shouldBe "htmlStyleWidget"
    }
}
