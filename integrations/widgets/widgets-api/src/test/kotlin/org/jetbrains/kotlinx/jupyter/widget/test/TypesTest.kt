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
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class TypesTest {
    private val widgetManager = TestWidgetManager.INSTANCE

    @Test
    fun `IntType should serialize to Int`() {
        IntType.serialize(42, widgetManager) shouldBe 42
    }

    @Test
    fun `IntType should deserialize from various numeric types`() {
        IntType.deserialize(42, widgetManager) shouldBe 42
        IntType.deserialize(42L, widgetManager) shouldBe 42
        IntType.deserialize(42.0, widgetManager) shouldBe 42
        IntType.deserialize(42.7, widgetManager) shouldBe 42
    }

    @Test
    fun `FloatType should serialize to Double`() {
        FloatType.serialize(3.14, widgetManager) shouldBe 3.14
    }

    @Test
    fun `FloatType should deserialize from various numeric types`() {
        FloatType.deserialize(3.14, widgetManager) shouldBe 3.14
        FloatType.deserialize(3, widgetManager) shouldBe 3.0
        FloatType.deserialize(3L, widgetManager) shouldBe 3.0
    }

    @Test
    fun `IntRangeType should serialize to list of bounds`() {
        IntRangeType.serialize(1..10, widgetManager) shouldBe listOf(1, 10)
    }

    @Test
    fun `IntRangeType should deserialize from list of bounds`() {
        IntRangeType.deserialize(listOf(1, 10), widgetManager) shouldBe 1..10
        IntRangeType.deserialize(listOf(1L, 10L), widgetManager) shouldBe 1..10
    }

    @Test
    fun `FloatRangeType should serialize to list of bounds`() {
        FloatRangeType.serialize(1.5..10.5, widgetManager) shouldBe listOf(1.5, 10.5)
    }

    @Test
    fun `FloatRangeType should deserialize from list of bounds`() {
        FloatRangeType.deserialize(listOf(1.5, 10.5), widgetManager) shouldBe 1.5..10.5
        FloatRangeType.deserialize(listOf(1, 10), widgetManager) shouldBe 1.0..10.0
    }

    @Test
    fun `PairType should serialize to list of elements`() {
        val pairType = PairType(IntType, StringType)
        pairType.serialize(42 to "hello", widgetManager) shouldBe listOf(42, "hello")
    }

    @Test
    fun `PairType should deserialize from list of elements`() {
        val pairType = PairType(IntType, StringType)
        pairType.deserialize(listOf(42, "hello"), widgetManager) shouldBe (42 to "hello")
    }

    @Test
    fun `ArrayType should serialize to list of elements`() {
        val arrayType = ArrayType(IntType)
        arrayType.serialize(listOf(1, 2, 3), widgetManager) shouldBe listOf(1, 2, 3)
    }

    @Test
    fun `ArrayType should deserialize from list of elements`() {
        val arrayType = ArrayType(IntType)
        arrayType.deserialize(listOf(1, 2, 3), widgetManager) shouldBe listOf(1, 2, 3)
        arrayType.deserialize(listOf(1L, 2L, 3L), widgetManager) shouldBe listOf(1, 2, 3)
    }

    @Test
    fun `NullableType should serialize values or null`() {
        val nullableType = NullableType(IntType)
        nullableType.serialize(42, widgetManager) shouldBe 42
        nullableType.serialize(null, widgetManager) shouldBe null
    }

    @Test
    fun `NullableType should deserialize values or null`() {
        val nullableType = NullableType(IntType)
        nullableType.deserialize(42, widgetManager) shouldBe 42
        nullableType.deserialize(null, widgetManager) shouldBe null
    }

    @Test
    fun `RawObjectType should serialize map as is`() {
        val obj = mapOf("a" to 1, "b" to "c")
        RawObjectType.serialize(obj, widgetManager) shouldBe obj
    }

    @Test
    fun `RawObjectType should deserialize map or return emptyMap for null`() {
        val obj = mapOf("a" to 1, "b" to "c")
        RawObjectType.deserialize(obj, widgetManager) shouldBe obj
        RawObjectType.deserialize(null, widgetManager) shouldBe emptyMap()
    }

    @Test
    fun `DatetimeType should serialize Instant to ISO-8601 string`() {
        val instant = Instant.parse("2023-01-01T12:00:00Z")
        DatetimeType.serialize(instant, widgetManager) shouldBe "2023-01-01T12:00:00Z"
    }

    @Test
    fun `DatetimeType should deserialize ISO-8601 string to Instant`() {
        val instant = Instant.parse("2023-01-01T12:00:00Z")
        DatetimeType.deserialize("2023-01-01T12:00:00Z", widgetManager) shouldBe instant
    }

    @Test
    fun `DateType should serialize LocalDate to ISO-8601 string`() {
        val date = LocalDate.of(2023, 1, 1)
        DateType.serialize(date, widgetManager) shouldBe "2023-01-01"
    }

    @Test
    fun `DateType should deserialize ISO-8601 string to LocalDate`() {
        val date = LocalDate.of(2023, 1, 1)
        DateType.deserialize("2023-01-01", widgetManager) shouldBe date
    }

    @Test
    fun `TimeType should serialize LocalTime to ISO-8601 string`() {
        val time = LocalTime.of(12, 0, 0)
        TimeType.serialize(time, widgetManager) shouldBe "12:00:00"
    }

    @Test
    fun `TimeType should deserialize ISO-8601 string to LocalTime`() {
        val time = LocalTime.of(12, 0, 0)
        TimeType.deserialize("12:00:00", widgetManager) shouldBe time
    }

    @Test
    fun `WidgetReferenceType should serialize widget to its IPY_MODEL_ prefixed ID`() {
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
    fun `WidgetReferenceType should deserialize IPY_MODEL_ prefixed ID to widget instance`() {
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
