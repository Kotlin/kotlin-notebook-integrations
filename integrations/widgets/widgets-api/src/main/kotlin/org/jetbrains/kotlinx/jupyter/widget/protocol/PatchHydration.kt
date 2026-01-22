package org.jetbrains.kotlinx.jupyter.widget.protocol

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * A "hydrated" patch containing widget state.
 * Unlike the raw JSON representation, this can contain [ByteArray] values
 * in place of binary data placeholders.
 */
public typealias Patch = Map<String, RawPropertyValue>

/**
 * Represents the raw message structure for Jupyter comm messages.
 * In Jupyter Widgets protocol, binary data is sent separately from the JSON state.
 */
internal data class WireMessage(
    /**
     * The JSON-serializable part of the state.
     */
    val state: JsonObject,
    /**
     * Paths within the [state] where binary buffers should be inserted.
     */
    val bufferPaths: List<BufferPath>,
    /**
     * The actual binary data.
     */
    val buffers: List<ByteArray>,
)

/**
 * Combines [WireMessage]'s state and buffers into a single "hydrated" [Patch].
 */
internal fun getPatch(wireMessage: WireMessage): Patch {
    val dehydratedMap = deserializeJsonMap(wireMessage.state)
    // Insert buffers into the map at specified paths
    for ((path, buf) in wireMessage.bufferPaths.zip(wireMessage.buffers)) {
        var obj: RawPropertyValue = RawPropertyValue.MapValue(dehydratedMap)
        for (key in path.dropLast(1)) {
            obj = getAt(obj, key)
        }
        setAt(obj, path.last(), RawPropertyValue.ByteArrayValue(buf))
    }
    return dehydratedMap
}

/**
 * Extracts binary buffers from a [Patch] and creates a [WireMessage].
 */
internal fun getWireMessage(patch: Patch): WireMessage {
    val pathStack = mutableListOf<BufferPathElement>()
    val bufferPaths = mutableListOf<BufferPath>()
    val buffers = mutableListOf<ByteArray>()

    // Recursively find all ByteArray values and their paths
    fun traverse(obj: RawPropertyValue) {
        when (obj) {
            is RawPropertyValue.MapValue -> {
                for ((key, value) in obj.values) {
                    pathStack.add(BufferPathElement.Key(key))
                    traverse(value)
                    pathStack.removeLast()
                }
            }
            is RawPropertyValue.ListValue -> {
                for (i in obj.values.indices) {
                    pathStack.add(BufferPathElement.Index(i))
                    traverse(obj.values[i])
                    pathStack.removeLast()
                }
            }
            is RawPropertyValue.ByteArrayValue -> {
                bufferPaths.add(pathStack.toList())
                buffers.add(obj.value)
            }
            else -> {}
        }
    }

    traverse(RawPropertyValue.MapValue(patch))
    // Serialize to JSON, ByteArrays will be replaced with JsonNull during serialization
    val state = serializeJsonMap(patch).jsonObject
    return WireMessage(state, bufferPaths, buffers)
}

private fun getAt(
    obj: RawPropertyValue,
    key: BufferPathElement,
): RawPropertyValue =
    when (obj) {
        is RawPropertyValue.MapValue -> {
            require(key is BufferPathElement.Key) { "Expected Key for MapValue, got $key" }
            obj.values[key.key] ?: error("Key ${key.key} not found in map")
        }
        is RawPropertyValue.ListValue -> {
            require(key is BufferPathElement.Index) { "Expected Index for ListValue, got $key" }
            obj.values[key.index]
        }
        else -> error("Unexpected object type: $obj")
    }

private fun setAt(
    obj: RawPropertyValue,
    key: BufferPathElement,
    value: RawPropertyValue,
) {
    when (obj) {
        is RawPropertyValue.MapValue -> {
            require(key is BufferPathElement.Key) { "Expected Key for MapValue, got $key" }
            @Suppress("UNCHECKED_CAST")
            (obj.values as MutableMap<String, RawPropertyValue>)[key.key] = value
        }

        is RawPropertyValue.ListValue -> {
            require(key is BufferPathElement.Index) { "Expected Index for ListValue, got $key" }
            @Suppress("UNCHECKED_CAST")
            (obj.values as MutableList<RawPropertyValue>)[key.index] = value
        }
        else -> error("Unexpected object type: $obj")
    }
}
