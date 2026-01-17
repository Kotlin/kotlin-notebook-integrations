package org.jetbrains.kotlinx.jupyter.widget.protocol

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.collections.get
import kotlin.collections.iterator

/**
 * A "hydrated" patch containing widget state.
 * Unlike the raw JSON representation, this can contain [ByteArray] values
 * in place of binary data placeholders.
 */
public typealias Patch = Map<String, Any?>

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
    val bufferPaths: List<List<Any>>,
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
        var obj: Any? = dehydratedMap
        for (key in path.dropLast(1)) obj = getAt(obj, key)
        setAt(obj, path.last(), buf)
    }
    return dehydratedMap
}

/**
 * Extracts binary buffers from a [Patch] and creates a [WireMessage].
 */
internal fun getWireMessage(patch: Patch): WireMessage {
    val pathStack = mutableListOf<Any>()
    val bufferPaths = mutableListOf<List<Any>>()
    val buffers = mutableListOf<ByteArray>()

    // Recursively find all ByteArray values and their paths
    fun traverse(obj: Any?) {
        when (obj) {
            is Map<*, *> -> {
                for ((key, value) in obj) {
                    pathStack.add(key as Any)
                    traverse(value)
                    pathStack.removeLast()
                }
            }
            is List<*> -> {
                for (i in obj.indices) {
                    pathStack.add(i)
                    traverse(obj[i])
                    pathStack.removeLast()
                }
            }
            is ByteArray -> {
                bufferPaths.add(pathStack.toList())
                buffers.add(obj)
            }
        }
    }

    traverse(patch)
    // Serialize to JSON, ByteArrays will be replaced with JsonNull during serialization
    val state = serializeJsonMap(patch).jsonObject
    return WireMessage(state, bufferPaths, buffers)
}

private fun getAt(
    obj: Any?,
    key: Any,
): Any? =
    when (obj) {
        is Map<*, *> -> obj[key as String]
        is List<*> -> obj[key as Int]
        else -> error("Unexpected object type: $obj")
    }

private fun setAt(
    obj: Any?,
    key: Any?,
    value: Any?,
) {
    when (obj) {
        is MutableMap<*, *> -> {
            @Suppress("UNCHECKED_CAST")
            (obj as MutableMap<Any?, Any?>)[key] = value
        }

        is MutableList<*> -> {
            @Suppress("UNCHECKED_CAST")
            (obj as MutableList<Any?>)[key as Int] = value
        }
        else -> error("Unexpected object type: $obj")
    }
}
