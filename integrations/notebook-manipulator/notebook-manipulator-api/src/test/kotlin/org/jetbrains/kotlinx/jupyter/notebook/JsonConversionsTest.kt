package org.jetbrains.kotlinx.jupyter.notebook

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.jupiter.api.Test

class JsonConversionsTest {
    @Test
    fun `should convert empty map to JsonObject`() {
        val map = emptyMap<String, Any?>()
        val result = mapToJson(map)

        result.shouldBeInstanceOf<JsonObject>()
        result.size shouldBe 0
    }

    @Test
    fun `should convert map with string values to JsonObject`() {
        val map = mapOf("key1" to "value1", "key2" to "value2")
        val result = mapToJson(map).jsonObject

        result["key1"]?.jsonPrimitive?.content shouldBe "value1"
        result["key2"]?.jsonPrimitive?.content shouldBe "value2"
    }

    @Test
    fun `should convert map with number values to JsonObject`() {
        val map = mapOf("int" to 42, "long" to 42L, "double" to 3.14)
        val result = mapToJson(map).jsonObject

        result["int"]?.jsonPrimitive?.int shouldBe 42
        result["long"]?.jsonPrimitive?.long shouldBe 42L
        result["double"]?.jsonPrimitive?.double shouldBe 3.14
    }

    @Test
    fun `should convert map with boolean values to JsonObject`() {
        val map = mapOf("true" to true, "false" to false)
        val result = mapToJson(map).jsonObject

        result["true"]?.jsonPrimitive?.boolean shouldBe true
        result["false"]?.jsonPrimitive?.boolean shouldBe false
    }

    @Test
    fun `should convert map with null values to JsonObject with JsonNull`() {
        val map = mapOf("key" to null)
        val result = mapToJson(map).jsonObject

        result["key"].shouldBeInstanceOf<JsonNull>()
    }

    @Test
    fun `should convert map with nested map to nested JsonObject`() {
        val map = mapOf("outer" to mapOf("inner" to "value"))
        val result = mapToJson(map).jsonObject

        val outerMap = result["outer"]?.jsonObject
        outerMap?.get("inner")?.jsonPrimitive?.content shouldBe "value"
    }

    @Test
    fun `should convert map with list to JsonObject with JsonArray`() {
        val map = mapOf("list" to listOf(1, 2, 3))
        val result = mapToJson(map).jsonObject

        val list = result["list"]?.jsonArray
        list?.size shouldBe 3
        list?.get(0)?.jsonPrimitive?.int shouldBe 1
        list?.get(1)?.jsonPrimitive?.int shouldBe 2
        list?.get(2)?.jsonPrimitive?.int shouldBe 3
    }

    @Test
    fun `should convert map with mixed types to JsonObject`() {
        val map =
            mapOf(
                "string" to "value",
                "int" to 42,
                "boolean" to true,
                "null" to null,
                "nested" to mapOf("key" to "value"),
                "list" to listOf(1, 2, 3),
            )
        val result = mapToJson(map).jsonObject

        result["string"]?.jsonPrimitive?.content shouldBe "value"
        result["int"]?.jsonPrimitive?.int shouldBe 42
        result["boolean"]?.jsonPrimitive?.boolean shouldBe true
        result["null"].shouldBeInstanceOf<JsonNull>()
        result["nested"].shouldBeInstanceOf<JsonObject>()
        result["list"].shouldBeInstanceOf<JsonArray>()
    }

    @Test
    fun `should convert unsupported types to string via toString`() {
        data class CustomObject(
            val value: String,
        )

        val map = mapOf("custom" to CustomObject("test"))
        val result = mapToJson(map).jsonObject

        // Should be converted via toString()
        (result["custom"]?.jsonPrimitive?.content?.contains("CustomObject") == true) shouldBe true
    }

    @Test
    fun `should convert empty list to JsonArray`() {
        val list = emptyList<Any?>()
        val result = listToJson(list)

        result.shouldBeInstanceOf<JsonArray>()
        result.size shouldBe 0
    }

    @Test
    fun `should convert list with primitive values to JsonArray`() {
        val list = listOf("string", 42, 3.14, true, null)
        val result = listToJson(list).jsonArray

        result[0].jsonPrimitive.content shouldBe "string"
        result[1].jsonPrimitive.int shouldBe 42
        result[2].jsonPrimitive.double shouldBe 3.14
        result[3].jsonPrimitive.boolean shouldBe true
        result[4].shouldBeInstanceOf<JsonNull>()
    }

    @Test
    fun `should convert list with nested list to nested JsonArray`() {
        val list = listOf(listOf(1, 2), listOf(3, 4))
        val result = listToJson(list).jsonArray

        val first = result[0].jsonArray
        first[0].jsonPrimitive.int shouldBe 1
        first[1].jsonPrimitive.int shouldBe 2

        val second = result[1].jsonArray
        second[0].jsonPrimitive.int shouldBe 3
        second[1].jsonPrimitive.int shouldBe 4
    }

    @Test
    fun `should convert list with nested map to JsonArray with JsonObject`() {
        val list = listOf(mapOf("key" to "value"))
        val result = listToJson(list).jsonArray

        val map = result[0].jsonObject
        map["key"]?.jsonPrimitive?.content shouldBe "value"
    }

    @Test
    fun `should convert empty JsonObject to empty map`() {
        val json = Json.parseToJsonElement("{}").jsonObject
        val result = jsonToMap(json)

        result.size shouldBe 0
    }

    @Test
    fun `should convert JsonObject with string values to map`() {
        val json = Json.parseToJsonElement("""{"key1": "value1", "key2": "value2"}""").jsonObject
        val result = jsonToMap(json)

        result["key1"] shouldBe "value1"
        result["key2"] shouldBe "value2"
    }

    @Test
    fun `should convert JsonObject with number values to map`() {
        val json = Json.parseToJsonElement("""{"int": 42, "double": 3.14}""").jsonObject
        val result = jsonToMap(json)

        result["int"] shouldBe 42L
        result["double"] shouldBe 3.14
    }

    @Test
    fun `should convert JsonObject with boolean values to map`() {
        val json = Json.parseToJsonElement("""{"true": true, "false": false}""").jsonObject
        val result = jsonToMap(json)

        result["true"] shouldBe true
        result["false"] shouldBe false
    }

    @Test
    fun `should convert JsonObject with null values to map with null`() {
        val json = Json.parseToJsonElement("""{"key": null}""").jsonObject
        val result = jsonToMap(json)

        result["key"].shouldBeNull()
    }

    @Test
    fun `should convert JsonObject with nested object to map with nested map`() {
        val json = Json.parseToJsonElement("""{"outer": {"inner": "value"}}""").jsonObject
        val result = jsonToMap(json)

        @Suppress("UNCHECKED_CAST")
        val outerMap = result["outer"] as Map<String, Any?>
        outerMap["inner"] shouldBe "value"
    }

    @Test
    fun `should convert JsonObject with array to map with list`() {
        val json = Json.parseToJsonElement("""{"list": [1, 2, 3]}""").jsonObject
        val result = jsonToMap(json)

        @Suppress("UNCHECKED_CAST")
        val list = result["list"] as List<Any?>
        list.size shouldBe 3
        list[0] shouldBe 1L
        list[1] shouldBe 2L
        list[2] shouldBe 3L
    }

    @Test
    fun `should handle JsonObject in jsonToAny`() {
        val json = Json.parseToJsonElement("""{"key": "value"}""")
        val result = jsonToAny(json)

        result.shouldBeInstanceOf<Map<*, *>>()
        @Suppress("UNCHECKED_CAST")
        (result as Map<String, Any?>)["key"] shouldBe "value"
    }

    @Test
    fun `should handle JsonArray in jsonToAny`() {
        val json = Json.parseToJsonElement("""[1, 2, 3]""")
        val result = jsonToAny(json)

        result.shouldBeInstanceOf<List<*>>()
        result.size shouldBe 3
        result[0] shouldBe 1L
        result[1] shouldBe 2L
        result[2] shouldBe 3L
    }

    @Test
    fun `should handle string primitive in jsonToAny`() {
        val json = Json.parseToJsonElement(""""test"""")
        val result = jsonToAny(json)

        result shouldBe "test"
    }

    @Test
    fun `should handle boolean primitive in jsonToAny`() {
        val jsonTrue = Json.parseToJsonElement("true")
        val jsonFalse = Json.parseToJsonElement("false")

        jsonToAny(jsonTrue) shouldBe true
        jsonToAny(jsonFalse) shouldBe false
    }

    @Test
    fun `should handle number primitives in jsonToAny`() {
        val jsonInt = Json.parseToJsonElement("42")
        val jsonDouble = Json.parseToJsonElement("3.14")

        jsonToAny(jsonInt) shouldBe 42L
        jsonToAny(jsonDouble) shouldBe 3.14
    }

    @Test
    fun `should handle null in jsonToAny`() {
        val json = Json.parseToJsonElement("null")
        val result = jsonToAny(json)

        result.shouldBeNull()
    }

    @Test
    fun `should roundtrip convert mapToJson and jsonToMap`() {
        val originalMap =
            mapOf(
                "string" to "value",
                "int" to 42,
                "double" to 3.14,
                "boolean" to true,
                "null" to null,
                "nested" to mapOf("key" to "value"),
                "list" to listOf(1, 2, 3),
            )

        val json = mapToJson(originalMap).jsonObject
        val resultMap = jsonToMap(json)

        resultMap["string"] shouldBe "value"
        resultMap["int"] shouldBe 42L
        resultMap["double"] shouldBe 3.14
        resultMap["boolean"] shouldBe true
        resultMap["null"].shouldBeNull()

        @Suppress("UNCHECKED_CAST")
        val nestedMap = resultMap["nested"] as Map<String, Any?>
        nestedMap["key"] shouldBe "value"

        @Suppress("UNCHECKED_CAST")
        val list = resultMap["list"] as List<Any?>
        list.size shouldBe 3
    }

    @Test
    fun `should convert deeply nested structure`() {
        val deepMap =
            mapOf(
                "level1" to
                    mapOf(
                        "level2" to
                            mapOf(
                                "level3" to listOf(1, 2, mapOf("level4" to "deep")),
                            ),
                    ),
            )

        val json = mapToJson(deepMap).jsonObject
        val resultMap = jsonToMap(json)

        @Suppress("UNCHECKED_CAST")
        val level1 = resultMap["level1"] as Map<String, Any?>

        @Suppress("UNCHECKED_CAST")
        val level2 = level1["level2"] as Map<String, Any?>

        @Suppress("UNCHECKED_CAST")
        val level3 = level2["level3"] as List<Any?>

        @Suppress("UNCHECKED_CAST")
        val level4 = level3[2] as Map<String, Any?>

        level4["level4"] shouldBe "deep"
    }
}
