package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.shouldBe
import org.jetbrains.kotlinx.jupyter.api.DisplayResult
import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetFactoryRegistry
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.FloatType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.IntType
import kotlin.test.Test

class PrimitiveTypesTest {
    private val widgetManager =
        object : WidgetManager {
            override val factoryRegistry: WidgetFactoryRegistry get() = notImplemented()

            override fun getWidget(modelId: String): WidgetModel = notImplemented()

            override fun getWidgetId(widget: WidgetModel): String = notImplemented()

            override fun registerWidget(widget: WidgetModel) = notImplemented()

            override fun renderWidget(widget: WidgetModel): DisplayResult = notImplemented()
        }

    @Test
    fun `int type should serialize`() {
        IntType.serialize(42, widgetManager) shouldBe 42
    }

    @Test
    fun `int type should deserialize`() {
        IntType.deserialize(42, widgetManager) shouldBe 42
        IntType.deserialize(42L, widgetManager) shouldBe 42
        IntType.deserialize(42.0, widgetManager) shouldBe 42
        IntType.deserialize(42.7, widgetManager) shouldBe 42
    }

    @Test
    fun `float type should serialize`() {
        FloatType.serialize(3.14, widgetManager) shouldBe 3.14
    }

    @Test
    fun `float type should deserialize`() {
        FloatType.deserialize(3.14, widgetManager) shouldBe 3.14
        FloatType.deserialize(3, widgetManager) shouldBe 3.0
        FloatType.deserialize(3L, widgetManager) shouldBe 3.0
    }

    companion object {
        private fun notImplemented(): Nothing = error("Not implemented")
    }
}
