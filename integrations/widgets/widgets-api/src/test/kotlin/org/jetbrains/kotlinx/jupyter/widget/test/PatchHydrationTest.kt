package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.kotlinx.jupyter.widget.protocol.BufferPath
import org.jetbrains.kotlinx.jupyter.widget.protocol.BufferPathElement
import org.jetbrains.kotlinx.jupyter.widget.protocol.RawPropertyValue
import org.jetbrains.kotlinx.jupyter.widget.protocol.WireMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.getPatch
import org.jetbrains.kotlinx.jupyter.widget.protocol.getWireMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.toPropertyValue
import org.junit.jupiter.api.Test

class PatchHydrationTest {
    @Test
    fun `getPatch should work for simple objects`() {
        val bytes = byteArrayOf(1, 2, 3)
        val state =
            buildJsonObject {
                put("v", null as String?)
            }
        val wire =
            WireMessage(
                state = state,
                bufferPaths = listOf(listOf(BufferPathElement.Key("v"))),
                buffers = listOf(bytes),
            )

        val patch = getPatch(wire)
        patch["v"] shouldBe RawPropertyValue.ByteArrayValue(bytes)
    }

    @Test
    fun `getPatch should work for nested objects`() {
        val bytes = byteArrayOf(4, 5, 6)
        val state =
            buildJsonObject {
                put(
                    "outer",
                    buildJsonObject {
                        put("inner", null as String?)
                    },
                )
            }
        val wire =
            WireMessage(
                state = state,
                bufferPaths = listOf(listOf(BufferPathElement.Key("outer"), BufferPathElement.Key("inner"))),
                buffers = listOf(bytes),
            )

        val patch = getPatch(wire)
        val outer = patch["outer"] as RawPropertyValue.MapValue
        outer.values["inner"] shouldBe RawPropertyValue.ByteArrayValue(bytes)
    }

    @Test
    fun `getWireMessage should work`() {
        val bytes1 = byteArrayOf(1)
        val bytes2 = byteArrayOf(2)
        val patch =
            mapOf(
                "a" to bytes1,
                "b" to
                    mapOf(
                        "c" to bytes2,
                        "d" to "string",
                    ),
            ).toPropertyValue() as RawPropertyValue.MapValue

        val wire = getWireMessage(patch.values)
        wire.buffers shouldBe listOf(bytes1, bytes2)
        val expectedBufferPaths: List<BufferPath> =
            listOf(
                listOf(BufferPathElement.Key("a")),
                listOf(BufferPathElement.Key("b"), BufferPathElement.Key("c")),
            )
        wire.bufferPaths shouldBe expectedBufferPaths

        wire.state["a"] shouldBe JsonNull
    }

    @Test
    fun `getWireMessage and getPatch roundtrip should preserve state`() {
        val patch =
            mapOf(
                "list" to
                    listOf(
                        1,
                        byteArrayOf(10, 20),
                        mapOf("x" to byteArrayOf(30)),
                    ),
            ).toPropertyValue() as RawPropertyValue.MapValue

        val wire = getWireMessage(patch.values)
        val hydrated = getPatch(wire)

        val list = (hydrated["list"] as RawPropertyValue.ListValue).values
        list[0] shouldBe RawPropertyValue.NumberValue(1)
        list[1] shouldBe RawPropertyValue.ByteArrayValue(byteArrayOf(10, 20))
        ((list[2] as RawPropertyValue.MapValue).values["x"]) shouldBe RawPropertyValue.ByteArrayValue(byteArrayOf(30))
    }
}
