package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.shouldBe
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.ArrayType
import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.NullableType
import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.PairType
import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.RawObjectType
import org.jetbrains.kotlinx.jupyter.widget.model.types.datetime.DateType
import org.jetbrains.kotlinx.jupyter.widget.model.types.datetime.DatetimeType
import org.jetbrains.kotlinx.jupyter.widget.model.types.datetime.TimeType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.FloatType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.IntType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.StringType
import org.jetbrains.kotlinx.jupyter.widget.model.types.ranges.FloatRangeType
import org.jetbrains.kotlinx.jupyter.widget.model.types.ranges.IntRangeType
import org.jetbrains.kotlinx.jupyter.widget.model.types.widget.WidgetReferenceType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test

class TypesTest {
    private val widgetManager = TestWidgetManager.INSTANCE

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

    @Test
    fun `int range type should serialize`() {
        IntRangeType.serialize(1..10, widgetManager) shouldBe listOf(1, 10)
    }

    @Test
    fun `int range type should deserialize`() {
        IntRangeType.deserialize(listOf(1, 10), widgetManager) shouldBe 1..10
        IntRangeType.deserialize(listOf(1L, 10L), widgetManager) shouldBe 1..10
    }

    @Test
    fun `float range type should serialize`() {
        FloatRangeType.serialize(1.5..10.5, widgetManager) shouldBe listOf(1.5, 10.5)
    }

    @Test
    fun `float range type should deserialize`() {
        FloatRangeType.deserialize(listOf(1.5, 10.5), widgetManager) shouldBe 1.5..10.5
        FloatRangeType.deserialize(listOf(1, 10), widgetManager) shouldBe 1.0..10.0
    }

    @Test
    fun `pair type should serialize`() {
        val pairType = PairType(IntType, StringType)
        pairType.serialize(42 to "hello", widgetManager) shouldBe listOf(42, "hello")
    }

    @Test
    fun `pair type should deserialize`() {
        val pairType = PairType(IntType, StringType)
        pairType.deserialize(listOf(42, "hello"), widgetManager) shouldBe (42 to "hello")
    }

    @Test
    fun `array type should serialize`() {
        val arrayType = ArrayType(IntType)
        arrayType.serialize(listOf(1, 2, 3), widgetManager) shouldBe listOf(1, 2, 3)
    }

    @Test
    fun `array type should deserialize`() {
        val arrayType = ArrayType(IntType)
        arrayType.deserialize(listOf(1, 2, 3), widgetManager) shouldBe listOf(1, 2, 3)
        arrayType.deserialize(listOf(1L, 2L, 3L), widgetManager) shouldBe listOf(1, 2, 3)
    }

    @Test
    fun `nullable type should serialize`() {
        val nullableType = NullableType(IntType)
        nullableType.serialize(42, widgetManager) shouldBe 42
        nullableType.serialize(null, widgetManager) shouldBe null
    }

    @Test
    fun `nullable type should deserialize`() {
        val nullableType = NullableType(IntType)
        nullableType.deserialize(42, widgetManager) shouldBe 42
        nullableType.deserialize(null, widgetManager) shouldBe null
    }

    @Test
    fun `raw object type should serialize`() {
        val obj = mapOf("a" to 1, "b" to "c")
        RawObjectType.serialize(obj, widgetManager) shouldBe obj
    }

    @Test
    fun `raw object type should deserialize`() {
        val obj = mapOf("a" to 1, "b" to "c")
        RawObjectType.deserialize(obj, widgetManager) shouldBe obj
        RawObjectType.deserialize(null, widgetManager) shouldBe emptyMap()
    }

    @Test
    fun `datetime type should serialize`() {
        val instant = Instant.parse("2023-01-01T12:00:00Z")
        DatetimeType.serialize(instant, widgetManager) shouldBe "2023-01-01T12:00:00Z"
    }

    @Test
    fun `datetime type should deserialize`() {
        val instant = Instant.parse("2023-01-01T12:00:00Z")
        DatetimeType.deserialize("2023-01-01T12:00:00Z", widgetManager) shouldBe instant
    }

    @Test
    fun `date type should serialize`() {
        val date = LocalDate.of(2023, 1, 1)
        DateType.serialize(date, widgetManager) shouldBe "2023-01-01"
    }

    @Test
    fun `date type should deserialize`() {
        val date = LocalDate.of(2023, 1, 1)
        DateType.deserialize("2023-01-01", widgetManager) shouldBe date
    }

    @Test
    fun `time type should serialize`() {
        val time = LocalTime.of(12, 0, 0)
        TimeType.serialize(time, widgetManager) shouldBe "12:00:00"
    }

    @Test
    fun `time type should deserialize`() {
        val time = LocalTime.of(12, 0, 0)
        TimeType.deserialize("12:00:00", widgetManager) shouldBe time
    }

    @Test
    fun `widget reference type should serialize`() {
        val widgetId = "test-widget-id"
        val myWidget = object : WidgetModel(widgetManager) {}
        val manager =
            object : TestWidgetManager {
                override fun getWidgetId(widget: WidgetModel): String? = if (widget === myWidget) widgetId else null
            }
        val refType = WidgetReferenceType<WidgetModel>()
        refType.serialize(myWidget, manager) shouldBe "IPY_MODEL_$widgetId"
    }

    @Test
    fun `widget reference type should deserialize`() {
        val widgetId = "test-widget-id"
        val widget = object : WidgetModel(widgetManager) {}
        val manager =
            object : TestWidgetManager {
                override fun getWidget(modelId: String): WidgetModel? = if (modelId == widgetId) widget else null
            }
        val refType = WidgetReferenceType<WidgetModel>()
        refType.deserialize("IPY_MODEL_$widgetId", manager) shouldBe widget
    }
}
