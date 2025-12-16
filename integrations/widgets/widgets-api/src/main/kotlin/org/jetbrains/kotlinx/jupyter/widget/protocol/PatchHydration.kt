package org.jetbrains.kotlinx.jupyter.widget.protocol

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.collections.get
import kotlin.collections.iterator

/**
 * Patch with inserted byte buffers - we call it "hydrated patch" or just "patch"
 * for the sake of simplicity
 */
public typealias Patch = Map<String, Any?>

internal data class WireMessage(
    val state: JsonObject,
    val bufferPaths: List<List<Any>>,
    val buffers: List<ByteArray>,
)

internal fun getPatch(wireMessage: WireMessage): Patch {
    val dehydratedMap = deserializeJsonMap(wireMessage.state)
    for ((path, buf) in wireMessage.bufferPaths.zip(wireMessage.buffers)) {
        var obj: Any? = dehydratedMap
        for (key in path.dropLast(1)) obj = getAt(obj, key)
        setAt(obj, path.last(), buf)
    }
    return dehydratedMap
}

internal fun getWireMessage(patch: Patch): WireMessage {
    val pathStack = mutableListOf<Any>()
    val bufferPaths = mutableListOf<List<Any>>()
    val buffers = mutableListOf<ByteArray>()

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
