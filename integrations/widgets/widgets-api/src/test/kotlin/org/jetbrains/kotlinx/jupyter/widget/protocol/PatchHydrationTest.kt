package org.jetbrains.kotlinx.jupyter.widget.protocol

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test

class PatchHydrationTest {
    @Test
    fun testGetPatchSimple() {
        val bytes = byteArrayOf(1, 2, 3)
        val state =
            buildJsonObject {
                put("v", null as String?)
            }
        val wire =
            WireMessage(
                state = state,
                bufferPaths = listOf(listOf("v")),
                buffers = listOf(bytes),
            )

        val patch = getPatch(wire)
        patch["v"] shouldBe bytes
    }

    @Test
    fun testGetPatchNested() {
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
                bufferPaths = listOf(listOf("outer", "inner")),
                buffers = listOf(bytes),
            )

        val patch = getPatch(wire)
        val outer = patch["outer"] as Map<*, *>
        outer["inner"] shouldBe bytes
    }

    @Test
    fun testGetWireMessage() {
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
            )

        val wire = getWireMessage(patch)
        wire.buffers shouldBe listOf(bytes1, bytes2)
        wire.bufferPaths shouldBe listOf(listOf("a"), listOf("b", "c"))

        wire.state["a"] shouldBe kotlinx.serialization.json.JsonNull
    }

    @Test
    fun testRoundtrip() {
        val patch =
            mapOf(
                "list" to
                    listOf(
                        1,
                        byteArrayOf(10, 20),
                        mapOf("x" to byteArrayOf(30)),
                    ),
            )

        val wire = getWireMessage(patch)
        val hydrated = getPatch(wire)

        val list = hydrated["list"] as List<Any?>
        list[0] shouldBe 1
        list[1] shouldBe byteArrayOf(10, 20)
        (list[2] as Map<*, *>)["x"] shouldBe byteArrayOf(30)
    }
}
