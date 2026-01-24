package org.jetbrains.kotlinx.jupyter.widget.protocol

/**
 * A tree-like type similar to JSON, but also supporting [ByteArray] as a leaf type.
 * Used for widget state synchronization.
 */
public sealed interface RawPropertyValue {
    public object Null : RawPropertyValue

    public data class StringValue(
        val value: String,
    ) : RawPropertyValue

    public data class NumberValue(
        val value: Number,
    ) : RawPropertyValue

    public data class BooleanValue(
        val value: Boolean,
    ) : RawPropertyValue

    public data class ByteArrayValue(
        val value: ByteArray,
    ) : RawPropertyValue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ByteArrayValue) return false
            return value.contentEquals(other.value)
        }

        override fun hashCode(): Int = value.contentHashCode()
    }

    public data class ListValue(
        val values: List<RawPropertyValue>,
    ) : RawPropertyValue

    public data class MapValue(
        val values: Map<String, RawPropertyValue>,
    ) : RawPropertyValue
}

internal fun String.toPropertyValue() = RawPropertyValue.StringValue(this)

internal fun Number.toPropertyValue() = RawPropertyValue.NumberValue(this)

internal fun Boolean.toPropertyValue() = RawPropertyValue.BooleanValue(this)

internal fun ByteArray.toPropertyValue() = RawPropertyValue.ByteArrayValue(this)

internal fun Map<*, *>.toPropertyValue(): RawPropertyValue =
    RawPropertyValue.MapValue(
        this
            .map { (k, v) ->
                val key = k as? String ?: error("Map key must be String, but was $k")
                key to v.toPropertyValue()
            }.toMap(),
    )

private fun List<*>.toPropertyValue(): RawPropertyValue =
    RawPropertyValue.ListValue(
        map { it.toPropertyValue() },
    )

private fun Any?.toPropertyValue(): RawPropertyValue =
    when (this) {
        null -> RawPropertyValue.Null
        is RawPropertyValue -> this
        is String -> toPropertyValue()
        is Number -> toPropertyValue()
        is Boolean -> toPropertyValue()
        is ByteArray -> toPropertyValue()
        is List<*> -> toPropertyValue()
        is Map<*, *> -> toPropertyValue()
        else -> error("Unsupported type for WidgetValue: ${this::class}")
    }

internal fun RawPropertyValue.MapValue.toRawValue(): Map<String, Any?> = values.mapValues { it.value.toRawValue() }

/**
 * Extension to convert [RawPropertyValue] back to basic Kotlin types (JSON-compatible + ByteArray).
 */
private fun RawPropertyValue.toRawValue(): Any? =
    when (this) {
        is RawPropertyValue.Null -> null
        is RawPropertyValue.StringValue -> value
        is RawPropertyValue.NumberValue -> value
        is RawPropertyValue.BooleanValue -> value
        is RawPropertyValue.ByteArrayValue -> value
        is RawPropertyValue.ListValue -> values.map { it.toRawValue() }
        is RawPropertyValue.MapValue -> toRawValue()
    }
